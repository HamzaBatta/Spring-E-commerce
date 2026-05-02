package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.requests.CreateOrderRequest;
import com.codewithmosh.store.dtos.resources.OrderResource;

/**
 * Strategy interface for order creation.
 *
 * Implementations differ in how they handle inventory reservation under concurrent access.
 * Switch between them at runtime via the X-Strategy request header.
 *
 * Registered implementations:
 *   @Component("default") — pessimistic locking (SELECT FOR UPDATE), safe under concurrency
 *   @Component("naive")   — no locking, demonstrates race conditions and data corruption
 *
 * Compare results via GET /metrics after a load test:
 *   label "order.create.pessimistic" vs "order.create.naive"
 */
public interface OrderCreationStrategy {
    OrderResource create(CreateOrderRequest request);
}
