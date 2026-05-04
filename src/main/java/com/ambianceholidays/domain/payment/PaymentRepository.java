package com.ambianceholidays.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByBookingId(UUID bookingId);
    /** Batch fetch for list views — caller picks the latest per booking. */
    List<Payment> findByBookingIdIn(List<UUID> bookingIds);
    Optional<Payment> findByStripePaymentIntent(String intent);
    Optional<Payment> findByPeachCheckoutId(String checkoutId);
}
