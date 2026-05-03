package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.Monitored;
import com.codewithmosh.store.dtos.requests.CreateOrderRequest;
import com.codewithmosh.store.dtos.resources.OrderResource;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.exceptions.InsufficientInventoryException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.exceptions.UserNotFoundException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import com.codewithmosh.store.repositories.UserRepository;
import com.codewithmosh.store.strategy.StrategySelector;
import lombok.AllArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default (safe) order creation using pessimistic locking.
 *
 * Inventory is reserved with SELECT FOR UPDATE — the database row is locked
 * for the duration of the transaction, preventing concurrent writes from
 * reading stale stock counts. Retries up to 3 times if the lock cannot
 * be acquired immediately.
 *
 * Use this as the baseline for your stress test comparison.
 * Send: X-Strategy: default  (or omit the header — "default" is the fallback)
 */
@Component("default-order")
@AllArgsConstructor
public class DefaultOrderCreationStrategy implements OrderCreationStrategy {

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final ProductRepository productRepository;
    private final StorageItemRepository storageItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StrategySelector strategySelector;

    private static final int MAX_LOCK_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 60L;

    @Monitored("order.create.pessimistic")
    @Transactional
    public OrderResource create(CreateOrderRequest request) {
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
            strategySelector.resolve(InvoiceProcessingStrategy.class).processInvoice(order.getId());
        } catch (Exception e) {
            System.err.println("Failed to enqueue/generate invoice: " + e.getMessage());
        }

        return orderMapper.toResource(order);
    }

    /**
     * Acquires a pessimistic write lock (SELECT FOR UPDATE) before decrementing stock.
     * Retries on lock contention with exponential-ish backoff.
     */
    private void reserveInventory(Long storageId, Long productId, Integer quantity) {
        for (int attempt = 1; attempt <= MAX_LOCK_RETRIES; attempt++) {
            try {
                var item = storageItemRepository.findForUpdate(storageId, productId).orElse(null);
                if (item == null) throw new InsufficientInventoryException();

                var current = item.getQuantity() == null ? 0 : item.getQuantity();
                if (current < quantity) throw new InsufficientInventoryException();

                item.setQuantity(current - quantity);
                storageItemRepository.save(item);
                return;
            } catch (PessimisticLockingFailureException ex) {
                if (attempt == MAX_LOCK_RETRIES) throw ex;
                try {
                    Thread.sleep(RETRY_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }
}
