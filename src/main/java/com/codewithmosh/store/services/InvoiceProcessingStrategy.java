package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.StrategyType;

@StrategyType("invoice")
public interface InvoiceProcessingStrategy {
    void processInvoice(Long orderId);
}
