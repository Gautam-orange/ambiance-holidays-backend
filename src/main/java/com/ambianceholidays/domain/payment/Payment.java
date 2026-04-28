package com.ambianceholidays.domain.payment;

import com.ambianceholidays.domain.booking.Booking;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "stripe_payment_intent", unique = true)
    private String stripePaymentIntent;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private PaymentMethod method = PaymentMethod.STRIPE;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(nullable = false, columnDefinition = "bpchar(3)")
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "refunded_cents", nullable = false)
    private int refundedCents = 0;

    @Column(name = "stripe_refund_id")
    private String stripeRefundId;

    @Column(name = "peach_checkout_id", unique = true)
    private String peachCheckoutId;

    @Column(name = "peach_payment_id")
    private String peachPaymentId;

    @Column(name = "peach_result_code")
    private String peachResultCode;

    @Column(name = "peach_result_desc")
    private String peachResultDesc;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public String getStripePaymentIntent() { return stripePaymentIntent; }
    public void setStripePaymentIntent(String stripePaymentIntent) { this.stripePaymentIntent = stripePaymentIntent; }
    public String getStripeChargeId() { return stripeChargeId; }
    public void setStripeChargeId(String stripeChargeId) { this.stripeChargeId = stripeChargeId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public int getRefundedCents() { return refundedCents; }
    public void setRefundedCents(int refundedCents) { this.refundedCents = refundedCents; }
    public String getStripeRefundId() { return stripeRefundId; }
    public void setStripeRefundId(String stripeRefundId) { this.stripeRefundId = stripeRefundId; }
    public String getPeachCheckoutId() { return peachCheckoutId; }
    public void setPeachCheckoutId(String peachCheckoutId) { this.peachCheckoutId = peachCheckoutId; }
    public String getPeachPaymentId() { return peachPaymentId; }
    public void setPeachPaymentId(String peachPaymentId) { this.peachPaymentId = peachPaymentId; }
    public String getPeachResultCode() { return peachResultCode; }
    public void setPeachResultCode(String peachResultCode) { this.peachResultCode = peachResultCode; }
    public String getPeachResultDesc() { return peachResultDesc; }
    public void setPeachResultDesc(String peachResultDesc) { this.peachResultDesc = peachResultDesc; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getRefundedAt() { return refundedAt; }
    public void setRefundedAt(Instant refundedAt) { this.refundedAt = refundedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
