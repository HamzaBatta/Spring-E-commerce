package com.codewithmosh.store.dtos.resources;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartProductResource {
    private Long id;
    private String name;
    private BigDecimal price;
}
