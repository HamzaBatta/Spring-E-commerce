package com.codewithmosh.store.services;

import com.codewithmosh.store.annotations.Monitored;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("async")
@AllArgsConstructor
public class AsyncInvoiceStrategy implements InvoiceProcessingStrategy {
    private final InvoiceProducer invoiceProducer;

    @Monitored("invoice.process.async")
    public void processInvoice(Long orderId) {
        invoiceProducer.sendInvoiceRequest(new InvoiceMessage(orderId));
    }
}
