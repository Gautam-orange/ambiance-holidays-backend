package com.ambianceholidays.api.booking.dto;

import com.ambianceholidays.domain.booking.BookingItemType;
import com.ambianceholidays.domain.cart.CartItem;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        BookingItemType itemType,
        UUID refId,
        String title,
        String imageUrl,
        short quantity,
        int unitPriceCents,
        int lineTotalCents,
        Map<String, Object> options,
        Instant expiresAt
) {
    public static CartItemResponse from(CartItem c) {
        return from(c, null, null);
    }

    public static CartItemResponse from(CartItem c, String title, String imageUrl) {
        return new CartItemResponse(c.getId(), c.getItemType(), c.getRefId(),
                title, imageUrl,
                c.getQuantity(),
                c.getUnitPriceCents(), c.getUnitPriceCents() * c.getQuantity(),
                c.getOptions(), c.getExpiresAt());
    }
}
