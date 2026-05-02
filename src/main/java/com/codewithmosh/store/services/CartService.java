package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.resources.CartItemResource;
import com.codewithmosh.store.dtos.resources.CartResource;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private CartRepository cartRepository;
    private CartMapper cartMapper;
    private ProductRepository productRepository;

    public CartResource createCart() {
        var cart = new Cart();
        cartRepository.save(cart);
        return cartMapper.toResource(cart);
    }

    public CartItemResource addToCart(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        var product = productRepository.findById(productId).orElse(null);
        if (product == null) throw new ProductNotFoundException();

        var cartItem = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toResource(cartItem);
    }

    public CartResource getCart(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();
        return cartMapper.toResource(cart);
    }

    public CartItemResource updateCart(UUID cartId, Long productId, Integer quantity) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        var cartItem = cart.getItem(productId);
        if (cartItem == null) throw new ProductNotFoundException();

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        return cartMapper.toResource(cartItem);
    }

    public void removeItem(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearCart(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();
        cart.clear();
        cartRepository.save(cart);
    }
}
