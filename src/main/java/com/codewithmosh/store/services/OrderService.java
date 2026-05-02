package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CreateOrderRequest;
import com.codewithmosh.store.dtos.OrderDto;
import com.codewithmosh.store.dtos.UpdateOrderStatusRequest;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.entities.StorageItem;
import com.codewithmosh.store.exceptions.InsufficientInventoryException;
import com.codewithmosh.store.exceptions.InvalidOrderStatusTransitionException;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.exceptions.UserNotFoundException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final StorageItemRepository storageItemRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllWithItems()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) {
            throw new OrderNotFoundException();
        }
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        var user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }

        var storage = storageRepository.findById(request.getStorageId()).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }

        var order = new Order();
        order.setUser(user);
        order.setStorage(storage);
        order.setStatus(OrderStatus.CONFIRMED);

        request.getItems().forEach(itemRequest -> {
            var product = productRepository.findById(itemRequest.getProductId()).orElse(null);
            if (product == null) {
                throw new ProductNotFoundException();
            }

            reserveInventory(storage.getId(), product.getId(), itemRequest.getQuantity());
            order.addItem(product, itemRequest.getQuantity(), product.getPrice());
        });

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) {
            throw new OrderNotFoundException();
        }

        var nextStatus = request.getStatus();
        if (!canTransition(order.getStatus(), nextStatus)) {
            throw new InvalidOrderStatusTransitionException();
        }

        if (nextStatus == OrderStatus.CANCELED && order.getStatus() != OrderStatus.CANCELED) {
            restockInventory(order);
        }

        order.setStatus(nextStatus);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) {
            throw new OrderNotFoundException();
        }

        if (order.getStatus() != OrderStatus.CANCELED) {
            restockInventory(order);
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        }
    }

    private void reserveInventory(Long storageId, Long productId, Integer quantity) {
        var item = storageItemRepository.findByStorageIdAndProductId(storageId, productId).orElse(null);
        if (item == null) {
            throw new InsufficientInventoryException();
        }

        var current = item.getQuantity() == null ? 0 : item.getQuantity();
        if (current < quantity) {
            throw new InsufficientInventoryException();
        }

        item.setQuantity(current - quantity);
        storageItemRepository.save(item);
    }

    private void restockInventory(Order order) {
        var storage = order.getStorage();
        if (storage == null) {
            return;
        }

        order.getItems().forEach(item -> {
            var storageItem = storageItemRepository
                    .findByStorageIdAndProductId(storage.getId(), item.getProduct().getId())
                    .orElseGet(() -> {
                        var newItem = new StorageItem();
                        newItem.setStorage(storage);
                        newItem.setProduct(item.getProduct());
                        newItem.setQuantity(0);
                        return newItem;
                    });

            var current = storageItem.getQuantity() == null ? 0 : storageItem.getQuantity();
            storageItem.setQuantity(current + item.getQuantity());
            storageItemRepository.save(storageItem);
        });
    }

    private boolean canTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }

        return switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELED;
            case SHIPPED -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELED;
            case DELIVERED, CANCELED -> false;
        };
    }
}

