package com.codewithmosh.store.services;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BatchReport {
    private long processedCount;
    private BigDecimal totalRevenue;
    private long durationMs;
    private String strategy;
}
