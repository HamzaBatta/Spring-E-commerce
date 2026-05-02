package com.codewithmosh.store.services;

import com.codewithmosh.store.config.StripeConfig;
import com.codewithmosh.store.dtos.requests.CreatePaymentRequest;
import com.codewithmosh.store.dtos.resources.PaymentResource;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.entities.Payment;
import com.codewithmosh.store.entities.PaymentStatus;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.mappers.PaymentMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final StripeConfig stripeConfig;

    @Transactional
    public PaymentResource createPaymentIntent(CreatePaymentRequest request) {
        var order = orderRepository.findWithItemsById(request.getOrderId()).orElse(null);
        if (order == null) throw new OrderNotFoundException();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentException("Order is not in PENDING status");
        }

        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new PaymentException("A payment already exists for this order");
        }

        BigDecimal total = order.getTotalPrice();
        long amountInCents = total.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .addPaymentMethodType("card")
                    .putMetadata("orderId", order.getId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            var payment = new Payment();
            payment.setOrder(order);
            payment.setStripePaymentIntentId(intent.getId());
            payment.setAmount(total);
            payment.setCurrency("usd");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            var resource = paymentMapper.toResource(payment);
            resource.setClientSecret(intent.getClientSecret());
            return resource;

        } catch (StripeException e) {
            throw new PaymentException("Failed to create payment: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResource confirmPayment(String paymentIntentId) {
        var payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        try {
            PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                    .setPaymentMethod("pm_card_visa")
                    .build();

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent = intent.confirm(params);

            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                paymentRepository.save(payment);
                confirmOrder(payment.getOrder());
            } else if ("canceled".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.CANCELED);
                paymentRepository.save(payment);
            }

            return paymentMapper.toResource(payment);

        } catch (StripeException e) {
            throw new PaymentException("Failed to confirm payment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResource getPaymentByOrderId(Long orderId) {
        var payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("No payment found for order " + orderId));
        return paymentMapper.toResource(payment);
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid webhook signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                var intentOptional = event.getDataObjectDeserializer().getObject();
                if (intentOptional.isPresent()) {
                    var intent = (PaymentIntent) intentOptional.get();
                    paymentRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        paymentRepository.save(payment);
                        confirmOrder(payment.getOrder());
                    });
                }
            }
            case "payment_intent.payment_failed" -> {
                var intentOptional = event.getDataObjectDeserializer().getObject();
                if (intentOptional.isPresent()) {
                    var intent = (PaymentIntent) intentOptional.get();
                    paymentRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                    });
                }
            }
            default -> { }
        }
    }

    private void confirmOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
}
