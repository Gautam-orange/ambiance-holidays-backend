package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TourRequest(
        UUID supplierId,
        @NotBlank String title,
        @NotNull TourCategory category,
        @NotNull TourRegion region,
        @NotNull TourDuration duration,
        BigDecimal durationHours,
        @NotBlank String description,
        @Min(0) int adultPriceCents,
        @Min(0) int childPriceCents,
        @Min(0) int infantPriceCents,
        @Min(1) short minPax,
        @Min(1) short maxPax,
        String[] includes,
        String[] excludes,
        String[] importantNotes,
        String coverImageUrl,
        String[] galleryUrls,
        TourStatus status,
        List<ItineraryStopRequest> itineraryStops,
        List<PickupZoneRequest> pickupZones
) {}
