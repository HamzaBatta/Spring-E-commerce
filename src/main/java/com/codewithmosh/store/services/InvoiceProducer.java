package com.codewithmosh.store.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InvoiceProducer {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(InvoiceProducer.class);

    public void sendInvoiceRequest(InvoiceMessage message) {
        log.info("Sending invoice message to queue for order {}", message.getOrderId());
        rabbitTemplate.convertAndSend(RabbitConfig.INVOICE_EXCHANGE, RabbitConfig.INVOICE_ROUTING, message);
    }
}
