package com.codewithmosh.store.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderConcurrencyTestRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Storage ID is required")
    private Long storageId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity must be at most 1000")
    private Integer quantity;

    @NotNull(message = "Requests count is required")
    @Min(value = 1, message = "Requests must be at least 1")
    @Max(value = 50, message = "Requests must be at most 50")
    private Integer requests;
}

