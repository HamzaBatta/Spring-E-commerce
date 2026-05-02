package com.codewithmosh.store.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
public class OrderCapacityLimiter {
    private final Semaphore semaphore;
    private final long timeoutMs;

    public OrderCapacityLimiter(
            @Value("${app.capacity.orders.maxConcurrent:4}") int maxConcurrent,
            @Value("${app.capacity.orders.timeoutMs:150}") long timeoutMs
    ) {
        this.semaphore = new Semaphore(Math.max(1, maxConcurrent), true);
        this.timeoutMs = Math.max(0L, timeoutMs);
    }

    public boolean tryAcquire() {
        try {
            if (timeoutMs == 0L) {
                return semaphore.tryAcquire();
            }
            return semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void release() {
        semaphore.release();
    }
}

