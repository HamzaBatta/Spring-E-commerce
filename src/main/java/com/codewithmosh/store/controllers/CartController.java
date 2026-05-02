package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.AddItemToCartRequest;
import com.codewithmosh.store.dtos.requests.UpdateCartItemRequest;
import com.codewithmosh.store.dtos.resources.CartItemResource;
import com.codewithmosh.store.dtos.resources.CartResource;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.services.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResource> createCart(UriComponentsBuilder uriBuilder) {
        var cartResource = cartService.createCart();
        var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartResource.getId()).toUri();
        return ResponseEntity.created(uri).body(cartResource);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemResource> addToCart(
            @PathVariable UUID cartId,
            @RequestBody AddItemToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(cartId, request.getProductId()));
    }

    @GetMapping("/{cartId}")
    public CartResource getCart(@PathVariable UUID cartId) {
        return cartService.getCart(cartId);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public CartItemResource updateItem(
            @PathVariable UUID cartId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateCart(cartId, productId, request.getQuantity());
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID cartId, @PathVariable Long productId) {
        cartService.removeItem(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cart not found"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Product not found in the cart"));
    }
}
