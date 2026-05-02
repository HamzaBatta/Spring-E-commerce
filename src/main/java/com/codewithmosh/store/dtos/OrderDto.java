package com.codewithmosh.store.dtos;

import com.codewithmosh.store.entities.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private Long storageId;
    private OrderStatus status;
    private List<com.codewithmosh.store.dtos.OrderItemDto> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
