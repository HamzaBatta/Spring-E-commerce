package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.CreatePaymentRequest;
import com.codewithmosh.store.dtos.resources.PaymentResource;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.services.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResource> createPayment(
            UriComponentsBuilder uriBuilder,
            @Valid @RequestBody CreatePaymentRequest request) {
        var resource = paymentService.createPaymentIntent(request);
        var uri = uriBuilder.path("/payments/order/{orderId}").buildAndExpand(resource.getOrderId()).toUri();
        return ResponseEntity.created(uri).body(resource);
    }

    @PostMapping("/{paymentIntentId}/confirm")
    public ResponseEntity<PaymentResource> confirmPayment(@PathVariable String paymentIntentId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentIntentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResource> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, String>> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
