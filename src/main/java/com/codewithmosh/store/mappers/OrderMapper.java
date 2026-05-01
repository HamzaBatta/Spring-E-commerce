package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.CartProductDto;
import com.codewithmosh.store.dtos.OrderDto;
import com.codewithmosh.store.dtos.OrderItemDto;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderItem;
import com.codewithmosh.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderDto toDto(Order order);

    @Mapping(target = "product", source = "product")
    @Mapping(target = "totalPrice", expression = "java(orderItem.getTotalPrice())")
    OrderItemDto toDto(OrderItem orderItem);

    CartProductDto toDto(Product product);
}

