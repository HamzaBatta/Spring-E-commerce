package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
