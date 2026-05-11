package com.ambianceholidays.api.booking.dto;

import com.ambianceholidays.domain.booking.BookingItem;
import com.ambianceholidays.domain.booking.BookingItemType;
import com.ambianceholidays.domain.car.Car;
import com.ambianceholidays.domain.car.CarRepository;

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
        String tripType,
        List<String> stops,
        List<ExtraResponse> extras,
        // CAR_RENTAL / CAR_TRANSFER enrichment so the admin & agent screens can
        // render route title, vehicle name, registration number, category and
        // routed distance without a separate /cars lookup.
        String carName,
        String carRegistrationNo,
        String carCategory,
        Integer distanceKm,
        String routeTitle
) {
    public record ExtraResponse(UUID id, String label, short quantity, int unitPriceCents, int totalCents) {}

    public static BookingItemResponse from(BookingItem item) {
        return from(item, null);
    }

    public static BookingItemResponse from(BookingItem item, CarRepository carRepo) {
        String carName = null, carRegistrationNo = null, carCategory = null;
        Integer distanceKm = null;
        if (carRepo != null && (item.getItemType() == BookingItemType.CAR_RENTAL
                || item.getItemType() == BookingItemType.CAR_TRANSFER)) {
            Car car = carRepo.findById(item.getRefId()).orElse(null);
            if (car != null) {
                carName = car.getName();
                carRegistrationNo = car.getRegistrationNo();
                carCategory = car.getCategory() != null ? car.getCategory().name() : null;
            }
        }
        // distanceKm is stored as a cart option; we don't have it on the entity
        // — it'll be derived via `notes` for hourly trips. Leave null; the UI
        // already shows pickup → dropoff label which conveys the route.
        String routeTitle = null;
        if (item.getPickupLocation() != null && item.getDropoffLocation() != null) {
            routeTitle = item.getPickupLocation() + " → " + item.getDropoffLocation();
        }
        return new BookingItemResponse(
                item.getId(), item.getItemType(), item.getRefId(), item.getQuantity(),
                item.getUnitPriceCents(), item.getTotalCents(), item.getServiceDate(),
                item.getStartAt(), item.getEndAt(), item.getPickupLocation(), item.getDropoffLocation(),
                item.getPaxAdults(), item.getPaxChildren(), item.getPaxInfants(),
                item.getNotes(), item.getRentalDays(),
                item.getTripType() != null ? item.getTripType().name() : null,
                item.getStops() != null ? List.of(item.getStops()) : List.of(),
                item.getExtras().stream().map(e -> new ExtraResponse(
                        e.getId(), e.getLabel(), e.getQuantity(), e.getUnitPriceCents(), e.getTotalCents())).toList(),
                carName, carRegistrationNo, carCategory, distanceKm, routeTitle
        );
    }
}
