package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.CreateOrderRequest;
import com.codewithmosh.store.dtos.requests.UpdateOrderStatusRequest;
import com.codewithmosh.store.dtos.resources.OrderResource;
import com.codewithmosh.store.exceptions.InsufficientInventoryException;
import com.codewithmosh.store.exceptions.InvalidOrderStatusTransitionException;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.exceptions.UserNotFoundException;
import com.codewithmosh.store.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public org.springframework.data.domain.Page<OrderResource> getAllOrders(
            @RequestParam(required = false, defaultValue = "0", name = "page") int page,
            @RequestParam(required = false, defaultValue = "10", name = "size") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResource> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResource> createOrder(
            UriComponentsBuilder uriBuilder,
            @Valid @RequestBody CreateOrderRequest request) {
        var created = orderService.createOrder(request);
        var uri = uriBuilder.path("/orders/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResource> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
    }

    @ExceptionHandler({UserNotFoundException.class, ProductNotFoundException.class, StorageNotFoundException.class,
            InsufficientInventoryException.class, InvalidOrderStatusTransitionException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
