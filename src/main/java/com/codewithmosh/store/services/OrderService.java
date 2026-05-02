package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.requests.CreateOrderRequest;
import com.codewithmosh.store.dtos.requests.UpdateOrderStatusRequest;
import com.codewithmosh.store.dtos.resources.OrderResource;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.entities.StorageItem;
import com.codewithmosh.store.exceptions.InsufficientInventoryException;
import com.codewithmosh.store.exceptions.InvalidOrderStatusTransitionException;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.SystemBusyException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.strategy.StrategySelector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final StorageItemRepository storageItemRepository;
    private final OrderMapper orderMapper;
    private final OrderCapacityLimiter orderCapacityLimiter;
    private final StrategySelector strategySelector;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderResource> getAllOrders(org.springframework.data.domain.Pageable pageable) {
        var page = orderRepository.findAllWithItems(pageable);
        return page.map(orderMapper::toResource);
    }

    @Transactional(readOnly = true)
    public OrderResource getOrder(Long id) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) throw new OrderNotFoundException();
        return orderMapper.toResource(order);
    }

    public OrderResource createOrder(CreateOrderRequest request) {
        if (!orderCapacityLimiter.tryAcquire()) throw new SystemBusyException();
        try {
            return strategySelector.resolve(OrderCreationStrategy.class).create(request);
        } finally {
            orderCapacityLimiter.release();
        }
    }

    @Transactional
    public OrderResource updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) throw new OrderNotFoundException();

        var nextStatus = request.getStatus();
        if (!canTransition(order.getStatus(), nextStatus)) throw new InvalidOrderStatusTransitionException();

        if (nextStatus == OrderStatus.CANCELED && order.getStatus() != OrderStatus.CANCELED) {
            restockInventory(order);
        }

        order.setStatus(nextStatus);
        orderRepository.save(order);
        return orderMapper.toResource(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        var order = orderRepository.findWithItemsById(id).orElse(null);
        if (order == null) throw new OrderNotFoundException();

        if (order.getStatus() != OrderStatus.CANCELED) {
            restockInventory(order);
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        }
    }

    private void restockInventory(Order order) {
        var storage = order.getStorage();
        if (storage == null) return;

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
        if (current == next) return true;
        return switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELED;
            case SHIPPED -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELED;
            case DELIVERED, CANCELED -> false;
        };
    }
}
