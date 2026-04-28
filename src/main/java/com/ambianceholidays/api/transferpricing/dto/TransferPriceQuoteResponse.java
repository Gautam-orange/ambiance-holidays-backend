package com.ambianceholidays.api.transferpricing.dto;

import java.util.UUID;

public record TransferPriceQuoteResponse(
        UUID tierId,
        String tierLabel,
        int distanceKm,
        int priceCents,
        boolean found          // false if no tier covers this distance
) {}
