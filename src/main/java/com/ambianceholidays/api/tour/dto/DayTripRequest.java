package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;
import jakarta.validation.constraints.*;

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
        Short maxPax,
        String[] includes,
        String[] excludes,
        String coverImageUrl,
        String[] galleryUrls,
        TourStatus status
) {}
