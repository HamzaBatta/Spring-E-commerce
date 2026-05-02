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
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.exceptions.UserNotFoundException;
import com.codewithmosh.store.exceptions.SystemBusyException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
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
    private final OrderCapacityLimiter orderCapacityLimiter;
    private final com.codewithmosh.store.strategy.StrategySelector strategySelector;

    private static final int MAX_LOCK_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 60L;

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

    @Transactional
    public OrderResource createOrder(CreateOrderRequest request) {
        var acquired = orderCapacityLimiter.tryAcquire();
        if (!acquired) {
            throw new SystemBusyException();
        }
        try {
            var user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) throw new UserNotFoundException();

            var storage = storageRepository.findById(request.getStorageId()).orElse(null);
            if (storage == null) throw new StorageNotFoundException();

            var order = new Order();
            order.setUser(user);
            order.setStorage(storage);
            order.setStatus(OrderStatus.PENDING);

            request.getItems().forEach(itemRequest -> {
                var product = productRepository.findById(itemRequest.getProductId()).orElse(null);
                if (product == null) throw new ProductNotFoundException();
                reserveInventory(storage.getId(), product.getId(), itemRequest.getQuantity());
                order.addItem(product, itemRequest.getQuantity(), product.getPrice());
            });

            orderRepository.save(order);

            // Trigger invoice generation using the active strategy. Fire-and-forget.
            try {
                strategySelector.resolve(com.codewithmosh.store.services.InvoiceProcessingStrategy.class).processInvoice(order.getId());
            } catch (Exception e) {
                // intentionally swallow so order creation is unaffected by invoice subsystem
                System.err.println("Failed to enqueue/generate invoice: " + e.getMessage());
            }

            return orderMapper.toResource(order);
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

    private void reserveInventory(Long storageId, Long productId, Integer quantity) {
        for (int attempt = 1; attempt <= MAX_LOCK_RETRIES; attempt++) {
            try {
                var item = storageItemRepository.findForUpdate(storageId, productId).orElse(null);
                if (item == null) {
                    throw new InsufficientInventoryException();
                }

                var current = item.getQuantity() == null ? 0 : item.getQuantity();
                if (current < quantity) {
                    throw new InsufficientInventoryException();
                }

                item.setQuantity(current - quantity);
                storageItemRepository.save(item);
                return;
            } catch (PessimisticLockingFailureException ex) {
                if (attempt == MAX_LOCK_RETRIES) {
                    throw ex;
                }
                try {
                    Thread.sleep(RETRY_BACKOFF_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
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
