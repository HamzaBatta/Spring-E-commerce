package com.codewithmosh.store.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Storage ID is required")
    private Long storageId;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<CreateOrderItemRequest> items;
}
