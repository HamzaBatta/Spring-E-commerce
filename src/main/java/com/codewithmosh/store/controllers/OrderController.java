package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.CreateOrderItemRequest;
import com.codewithmosh.store.dtos.requests.CreateOrderRequest;
import com.codewithmosh.store.dtos.requests.OrderConcurrencyTestRequest;
import com.codewithmosh.store.dtos.requests.UpdateOrderStatusRequest;
import com.codewithmosh.store.dtos.resources.OrderResource;
import com.codewithmosh.store.exceptions.InsufficientInventoryException;
import com.codewithmosh.store.exceptions.InvalidOrderStatusTransitionException;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.exceptions.SystemBusyException;
import com.codewithmosh.store.exceptions.UserNotFoundException;
import com.codewithmosh.store.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResource> getAllOrders() {
        return orderService.getAllOrders();
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

    @PostMapping("/test/concurrency")
    public ResponseEntity<Map<String, Object>> testOrderConcurrency(
            @Valid @RequestBody OrderConcurrencyTestRequest request
    ) {
        var requested = request.getRequests();
        var success = new AtomicInteger();
        var busy = new AtomicInteger();
        var insufficient = new AtomicInteger();
        var other = new AtomicInteger();

        var startLatch = new CountDownLatch(1);
        var doneLatch = new CountDownLatch(requested);
        ExecutorService executor = Executors.newFixedThreadPool(requested);

        for (int i = 0; i < requested; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    var orderRequest = new com.codewithmosh.store.dtos.requests.CreateOrderRequest();
                    orderRequest.setUserId(request.getUserId());
                    orderRequest.setStorageId(request.getStorageId());

                    var item = new CreateOrderItemRequest();
                    item.setProductId(request.getProductId());
                    item.setQuantity(request.getQuantity());
                    orderRequest.setItems(Collections.singletonList(item));

                    orderService.createOrder(orderRequest);
                    success.incrementAndGet();
                } catch (SystemBusyException ex) {
                    busy.incrementAndGet();
                } catch (InsufficientInventoryException ex) {
                    insufficient.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    other.incrementAndGet();
                } catch (RuntimeException ex) {
                    other.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        try {
            doneLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Concurrency test interrupted"));
        } finally {
            executor.shutdownNow();
        }

        return ResponseEntity.ok(Map.of(
                "requested", requested,
                "success", success.get(),
                "busy", busy.get(),
                "insufficientInventory", insufficient.get(),
                "otherErrors", other.get()
        ));
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

    @ExceptionHandler(SystemBusyException.class)
    public ResponseEntity<Map<String, String>> handleSystemBusy(SystemBusyException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", ex.getMessage()));
    }
}
