package com.ambianceholidays.api.webhook;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingRepository;
import com.ambianceholidays.domain.booking.BookingStatus;
import com.ambianceholidays.domain.payment.Payment;
import com.ambianceholidays.domain.payment.PaymentRepository;
import com.ambianceholidays.domain.payment.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final String IDEMPOTENCY_KEY_PREFIX = "stripe:event:";

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final StringRedisTemplate redis;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(PaymentRepository paymentRepo, BookingRepository bookingRepo,
            StringRedisTemplate redis) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.redis = redis;
    }

    @PostMapping
    public ResponseEntity<String> handleEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Idempotency guard — skip already-processed events
        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + event.getId();
        Boolean isNew = redis.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate Stripe event ignored: {}", event.getId());
            return ResponseEntity.ok("ok");
        }

        log.info("Stripe event received: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            case "payment_intent.canceled" -> handlePaymentCanceled(event);
            case "charge.refunded" -> handleRefunded(event);
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }

    private void handlePaymentSucceeded(Event event) {
        extractPaymentIntent(event).ifPresent(pi -> {
            paymentRepo.findByStripePaymentIntent(pi.getId()).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setStripeChargeId(pi.getLatestCharge());
                payment.setPaidAt(Instant.now());
                paymentRepo.save(payment);

                Booking booking = payment.getBooking();
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepo.save(booking);
                log.info("Payment succeeded for booking {}", booking.getReference());
            });
        });
    }

    private void handlePaymentFailed(Event event) {
        extractPaymentIntent(event).ifPresent(pi ->
            paymentRepo.findByStripePaymentIntent(pi.getId()).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(payment);
                log.info("Payment failed for intent {}", pi.getId());
            })
        );
    }

    private void handlePaymentCanceled(Event event) {
        extractPaymentIntent(event).ifPresent(pi ->
            paymentRepo.findByStripePaymentIntent(pi.getId()).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(payment);
            })
        );
    }

    private void handleRefunded(Event event) {
        try {
            Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
            if (charge == null || charge.getPaymentIntent() == null) return;
            paymentRepo.findByStripePaymentIntent(charge.getPaymentIntent()).ifPresent(payment -> {
                long amountRefunded = charge.getAmountRefunded() != null ? charge.getAmountRefunded() : 0;
                long amountCharged  = charge.getAmount()         != null ? charge.getAmount()         : 0;
                payment.setStatus(amountRefunded >= amountCharged
                        ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
                payment.setRefundedCents((int) amountRefunded);
                payment.setRefundedAt(Instant.now());
                paymentRepo.save(payment);
            });
        } catch (Exception e) {
            log.warn("Failed to process charge.refunded event {}: {}", event.getId(), e.getMessage());
        }
    }

    private Optional<PaymentIntent> extractPaymentIntent(Event event) {
        try {
            return Optional.ofNullable((PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null));
        } catch (Exception e) {
            log.warn("Failed to extract PaymentIntent from event {}: {}", event.getId(), e.getMessage());
            return Optional.empty();
        }
    }
}
