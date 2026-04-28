package com.ambianceholidays.api.transferpricing.dto;

import com.ambianceholidays.domain.transferpricing.TransferPricingTier;

import java.util.UUID;

public record TransferPricingTierResponse(
        UUID id,
        String label,
        int minKm,
        Integer maxKm,
        int priceCents,
        boolean active,
        short sortOrder
) {
    public static TransferPricingTierResponse from(TransferPricingTier t) {
        return new TransferPricingTierResponse(
                t.getId(), t.getLabel(), t.getMinKm(), t.getMaxKm(),
                t.getPriceCents(), t.isActive(), t.getSortOrder()
        );
    }
}
