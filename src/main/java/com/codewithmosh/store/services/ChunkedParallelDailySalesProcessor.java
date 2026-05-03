package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.Monitored;
import com.codewithmosh.store.repositories.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component("chunked")
public class ChunkedParallelDailySalesProcessor implements DailySalesProcessor {

    public ChunkedParallelDailySalesProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private final OrderRepository orderRepository;

    @Value("${app.batch.chunk-size:50}")
    private int chunkSize;

    @Value("${app.batch.pool-size:4}")
    private int poolSize;

    @Override
    @Monitored("batch.dailySales.chunked")
    public BatchReport process() {
        long start = System.currentTimeMillis();
        AtomicLong processed = new AtomicLong(0);
        final Object lock = new Object();
        final BigDecimal[] total = new BigDecimal[]{BigDecimal.ZERO};

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, poolSize));
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Pageable pageable = PageRequest.of(0, chunkSize);
        Page<com.codewithmosh.store.entities.Order> page;

        do {
            page = orderRepository.findAllWithItems(pageable);
            if (page.isEmpty()) break;

            List<com.codewithmosh.store.entities.Order> orders = page.getContent();
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                BigDecimal local = orders.stream()
                        .map(com.codewithmosh.store.entities.Order::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                synchronized (lock) {
                    total[0] = total[0].add(local);
                }
                processed.addAndGet(orders.size());
            }, executor);

            futures.add(f);
            pageable = page.hasNext() ? page.nextPageable() : null;
        } while (pageable != null);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - start;
        return new BatchReport(processed.get(), total[0], duration, "chunked");
    }
}
