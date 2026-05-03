package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.Monitored;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("default-daily-sales")
@AllArgsConstructor
public class SimpleDailySalesProcessor implements DailySalesProcessor {

    private final OrderRepository orderRepository;

    @Override
    @Monitored("batch.dailySales.simple")
    public BatchReport process() {
        long start = System.currentTimeMillis();
        List<com.codewithmosh.store.entities.Order> orders = orderRepository.findAllWithItems();
        BigDecimal total = orders.stream()
                .map(com.codewithmosh.store.entities.Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long duration = System.currentTimeMillis() - start;
        return new BatchReport(orders.size(), total, duration, "simple");
    }
}
