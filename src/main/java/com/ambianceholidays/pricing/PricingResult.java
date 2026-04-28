package com.ambianceholidays.pricing;

public record PricingResult(
        int subtotalCents,
        int markupCents,
        int commissionCents,
        int vatBaseCents,
        int vatCents,
        int totalCents
) {}
