package com.codewithmosh.store.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
}
