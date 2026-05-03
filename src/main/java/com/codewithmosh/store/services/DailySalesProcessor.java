package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.StrategyType;

@StrategyType("daily-sales")
public interface DailySalesProcessor {
    BatchReport process();
}
