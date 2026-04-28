package com.ambianceholidays.api.booking.dto;

import com.ambianceholidays.domain.booking.BookingItem;
import com.ambianceholidays.domain.booking.BookingItemType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookingItemResponse(
        UUID id,
        BookingItemType itemType,
        UUID refId,
        short quantity,
        int unitPriceCents,
        int totalCents,
        LocalDate serviceDate,
        Instant startAt,
        Instant endAt,
        String pickupLocation,
        String dropoffLocation,
        short paxAdults,
        short paxChildren,
        short paxInfants,
        String notes,
        Short rentalDays,
        List<ExtraResponse> extras
) {
    public record ExtraResponse(UUID id, String label, short quantity, int unitPriceCents, int totalCents) {}

    public static BookingItemResponse from(BookingItem item) {
        return new BookingItemResponse(
                item.getId(), item.getItemType(), item.getRefId(), item.getQuantity(),
                item.getUnitPriceCents(), item.getTotalCents(), item.getServiceDate(),
                item.getStartAt(), item.getEndAt(), item.getPickupLocation(), item.getDropoffLocation(),
                item.getPaxAdults(), item.getPaxChildren(), item.getPaxInfants(),
                item.getNotes(), item.getRentalDays(),
                item.getExtras().stream().map(e -> new ExtraResponse(
                        e.getId(), e.getLabel(), e.getQuantity(), e.getUnitPriceCents(), e.getTotalCents())).toList()
        );
    }
}
