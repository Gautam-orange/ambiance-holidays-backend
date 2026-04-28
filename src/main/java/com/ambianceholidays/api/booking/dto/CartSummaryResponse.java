package com.ambianceholidays.api.booking.dto;

import java.util.List;

public record CartSummaryResponse(
        List<CartItemResponse> items,
        int subtotalCents,
        int vatCents,
        int markupCents,
        int agentCommissionCents,
        java.math.BigDecimal markupPercent,
        int totalCents,
        int itemCount
) {}
