package com.codewithmosh.store.dtos.resources;

import com.codewithmosh.store.entities.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderResource {
    private Long id;
    private Long userId;
    private Long storageId;
    private OrderStatus status;
    private List<OrderItemResource> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
