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
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Naive (unsafe) order creation — no locking on inventory.
 *
 * Reads the current stock, checks it, then writes the decremented value back
 * as a plain UPDATE with no lock held between the read and write.
 *
 * Under concurrent load two threads can both read the same stock value,
 * both pass the availability check, and both decrement — resulting in
 * overselling (stock going negative) or lost updates.
 *
 * PURPOSE: This implementation exists purely for benchmarking and to demonstrate
 * the race condition. Never use it in production.
 *
 * Send: X-Strategy: naive
 * Compare vs "default" in GET /metrics after a concurrent load test.
 */
@Component("naive")
@AllArgsConstructor
public class NaiveOrderCreationStrategy implements OrderCreationStrategy {

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final ProductRepository productRepository;
    private final StorageItemRepository storageItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StrategySelector strategySelector;
    private final MeterRegistry meterRegistry;

    @Monitored("order.create.naive")
    @Transactional
    public OrderResource create(CreateOrderRequest request) {
        try {
            return meterRegistry.timer("order.simple").recordCallable(() -> {
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

                    // No lock — plain read → check → write. Race condition is possible here.
                    var item = storageItemRepository
                            .findByStorageIdAndProductId(storage.getId(), product.getId())
                            .orElseThrow(InsufficientInventoryException::new);

                    var current = item.getQuantity() == null ? 0 : item.getQuantity();
                    if (current < itemRequest.getQuantity()) throw new InsufficientInventoryException();

                    item.setQuantity(current - itemRequest.getQuantity());
                    storageItemRepository.save(item);

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
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }
}
