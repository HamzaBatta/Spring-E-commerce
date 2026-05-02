package com.codewithmosh.store.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InvoiceConsumer {
    private final InvoiceService invoiceService;
    private static final Logger log = LoggerFactory.getLogger(InvoiceConsumer.class);

    @RabbitListener(queues = RabbitConfig.INVOICE_QUEUE)
    public void handleInvoiceMessage(InvoiceMessage message) {
        log.info("Received invoice message for order {}", message.getOrderId());
        try {
            invoiceService.generateInvoice(message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process invoice for order {}", message.getOrderId(), e);
            // Consider retrying or sending to a dead-letter queue in production
        }
    }
}
