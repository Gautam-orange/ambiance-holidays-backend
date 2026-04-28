package com.ambianceholidays.api.transferpricing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferPricingTierRequest(
        @NotBlank String label,
        @NotNull @Min(0) Integer minKm,
        Integer maxKm,          // null = unlimited
        @NotNull @Min(0) Integer priceCents,
        boolean active,
        short sortOrder
) {}
