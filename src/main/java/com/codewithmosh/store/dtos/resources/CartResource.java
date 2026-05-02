package com.codewithmosh.store.dtos.resources;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CartResource {
    private UUID id;
    private List<CartItemResource> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
