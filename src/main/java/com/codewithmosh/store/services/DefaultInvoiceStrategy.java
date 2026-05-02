package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.Monitored;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("default")
@AllArgsConstructor
public class DefaultInvoiceStrategy implements InvoiceProcessingStrategy {
    private final InvoiceService invoiceService;

    @Monitored("invoice.process.sync")
    public void processInvoice(Long orderId) {
        invoiceService.generateInvoice(orderId);
    }
}
