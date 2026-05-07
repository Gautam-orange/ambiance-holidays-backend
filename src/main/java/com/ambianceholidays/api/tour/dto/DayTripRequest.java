package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DayTripRequest(
        UUID supplierId,
        @NotBlank String title,
        @NotNull DayTripType tripType,
        @NotNull TourRegion region,
        @NotNull TourDuration duration,
        String description,
        @Min(0) int adultPriceCents,
        @Min(0) int childPriceCents,
        @Min(0) Integer pricePerVehicleCents,
        @Min(0) Integer netRatePerPaxCents,
        BigDecimal markupPct,
        Short maxPax,
        String[] includes,
        String[] excludes,
        String coverImageUrl,
        String[] galleryUrls,
        String theme,
        String availabilityMode,
        TourStatus status,
        List<HighlightRequest> highlights,
        List<ItineraryStopRequest> itineraryStops,
        List<PickupZoneRequest> pickupZones
) {
    public record HighlightRequest(@NotBlank String text, Short displayOrder) {}

    public record ItineraryStopRequest(
            Short stopOrder,
            @NotBlank String title,
            String timeLabel,
            String location,
            String description
    ) {}

    public record PickupZoneRequest(
            @NotBlank String zoneName,
            String hotelName,
            String pickupFrom,
            String pickupTo,
            Short sortOrder
    ) {}
}
