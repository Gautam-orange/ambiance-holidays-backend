package com.ambianceholidays.api.booking.dto;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingStatus;
import com.ambianceholidays.domain.payment.Payment;
import com.ambianceholidays.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String reference,
        BookingStatus status,
        String customerName,
        String customerEmail,
        String customerPhone,
        String agentName,
        UUID agentId,
        String createdByName,
        boolean isEnquiry,
        String cancelledByType,
        LocalDate serviceDate,
        int subtotalCents,
        int markupCents,
        int commissionCents,
        int vatCents,
        int totalCents,
        BigDecimal vatRate,
        BigDecimal markupRate,
        BigDecimal commissionRate,
        String specialRequests,
        String cancelReason,
        int cancellationFeeCents,
        Instant cancelledAt,
        Instant createdAt,
        Instant updatedAt,
        List<BookingItemResponse> items,
        PaymentSummary payment
) {
    /** Backwards-compat factory for callers that don't need payment info. */
    public static BookingResponse from(Booking b) {
        return from(b, null);
    }

    public static BookingResponse from(Booking b, Payment p) {
        String customerName = b.getCustomer().getFullName();
        String customerPhone = b.getCustomer().getPhone();
        String agentName = b.getAgent() != null ? b.getAgent().getCompanyName() : null;
        UUID agentId = b.getAgent() != null ? b.getAgent().getId() : null;
        String createdByName = b.getCreatedBy() != null ? b.getCreatedBy().getFullName() : null;
        return new BookingResponse(
                b.getId(), b.getReference(), b.getStatus(),
                customerName, b.getCustomer().getEmail(), customerPhone,
                agentName, agentId, createdByName,
                b.isEnquiry(), b.getCancelledByType(),
                b.getServiceDate(),
                b.getSubtotalCents(), b.getMarkupCents(), b.getCommissionCents(),
                b.getVatCents(), b.getTotalCents(),
                b.getVatRate(), b.getMarkupRate(), b.getCommissionRate(),
                b.getSpecialRequests(), b.getCancelReason(), b.getCancellationFeeCents(),
                b.getCancelledAt(), b.getCreatedAt(), b.getUpdatedAt(),
                b.getItems().stream().map(BookingItemResponse::from).toList(),
                p != null ? PaymentSummary.from(p) : null
        );
    }

    /** Lightweight payment view for admin booking detail screens. */
    public record PaymentSummary(
            UUID id,
            PaymentStatus status,
            String method,
            int amountCents,
            int refundedCents,
            String currency,
            String peachCheckoutId,
            String peachPaymentId,
            String peachResultCode,
            String peachResultDesc,
            Instant paidAt,
            Instant refundedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PaymentSummary from(Payment p) {
            return new PaymentSummary(
                    p.getId(), p.getStatus(),
                    p.getMethod() != null ? p.getMethod().name() : null,
                    p.getAmountCents(), p.getRefundedCents(), p.getCurrency(),
                    p.getPeachCheckoutId(), p.getPeachPaymentId(),
                    p.getPeachResultCode(), p.getPeachResultDesc(),
                    p.getPaidAt(), p.getRefundedAt(),
                    p.getCreatedAt(), p.getUpdatedAt()
            );
        }
    }
}
