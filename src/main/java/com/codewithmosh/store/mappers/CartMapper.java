package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.resources.CartItemResource;
import com.codewithmosh.store.dtos.resources.CartResource;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartResource toResource(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemResource toResource(CartItem cartItem);
}
