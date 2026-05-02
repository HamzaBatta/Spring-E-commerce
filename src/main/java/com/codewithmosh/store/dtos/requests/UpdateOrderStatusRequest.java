package com.codewithmosh.store.dtos.requests;

import com.codewithmosh.store.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus status;
}
