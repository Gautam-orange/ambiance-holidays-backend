package com.ambianceholidays.api.transfer.dto;

import com.ambianceholidays.domain.car.CarCategory;
import com.ambianceholidays.domain.transfer.TransferTripType;
import jakarta.validation.constraints.*;

public record TransferRouteRequest(
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        @NotNull TransferTripType tripType,
        @NotNull CarCategory carCategory,
        @Min(0) int basePriceCents,
        Short estDurationMins,
        Short estKm,
        boolean active
) {}
