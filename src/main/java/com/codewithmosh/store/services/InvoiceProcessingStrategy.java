package com.codewithmosh.store.services;

public interface InvoiceProcessingStrategy {
    void processInvoice(Long orderId);
}
