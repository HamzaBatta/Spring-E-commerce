package com.codewithmosh.store.dtos.resources;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResource {
    private CartProductResource product;
    private Integer quantity;
    private BigDecimal totalPrice;
}
