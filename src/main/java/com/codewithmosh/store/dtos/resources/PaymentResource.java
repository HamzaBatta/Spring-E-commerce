package com.codewithmosh.store.dtos.resources;

import com.codewithmosh.store.entities.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResource {
    private Long id;
    private Long orderId;
    private String stripePaymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
}
