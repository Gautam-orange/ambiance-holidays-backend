package com.ambianceholidays.api.transferpricing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TransferPricingTierRequest(
        @NotBlank String label,
        @NotNull @Min(0) Integer minKm,
        Integer maxKm,          // null = unlimited
        @NotNull @Min(0) Integer priceCents,
        boolean active,
        short sortOrder,
        // What's Included / Excluded — bullet lists shown on the customer-facing
        // transfer detail page. Either field can be null/empty for tiers that
        // don't need them.
        List<String> includes,
        List<String> excludes
) {}
