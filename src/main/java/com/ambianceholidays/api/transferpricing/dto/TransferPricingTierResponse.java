package com.ambianceholidays.api.transferpricing.dto;

import com.ambianceholidays.domain.transferpricing.TransferPricingTier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record TransferPricingTierResponse(
        UUID id,
        String label,
        int minKm,
        Integer maxKm,
        int priceCents,
        boolean active,
        short sortOrder,
        List<String> includes,
        List<String> excludes
) {
    public static TransferPricingTierResponse from(TransferPricingTier t) {
        return new TransferPricingTierResponse(
                t.getId(), t.getLabel(), t.getMinKm(), t.getMaxKm(),
                t.getPriceCents(), t.isActive(), t.getSortOrder(),
                t.getIncludes() == null ? Collections.emptyList() : Arrays.asList(t.getIncludes()),
                t.getExcludes() == null ? Collections.emptyList() : Arrays.asList(t.getExcludes())
        );
    }
}
