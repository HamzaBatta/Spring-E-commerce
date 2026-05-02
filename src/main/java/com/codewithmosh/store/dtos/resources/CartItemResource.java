package com.codewithmosh.store.dtos.resources;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResource {
    private CartProductResource product;
    private int quantity;
    private BigDecimal totalPrice;
}
