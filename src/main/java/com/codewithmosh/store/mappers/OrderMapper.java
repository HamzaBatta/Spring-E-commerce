package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.resources.CartProductResource;
import com.codewithmosh.store.dtos.resources.OrderItemResource;
import com.codewithmosh.store.dtos.resources.OrderResource;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderItem;
import com.codewithmosh.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "storageId", source = "storage.id")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderResource toResource(Order order);

    @Mapping(target = "product", source = "product")
    @Mapping(target = "totalPrice", expression = "java(orderItem.getTotalPrice())")
    OrderItemResource toResource(OrderItem orderItem);

    CartProductResource toResource(Product product);
}
