package com.ambianceholidays.api.booking.dto;

import com.ambianceholidays.domain.booking.BookingItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record AddCartItemRequest(
        @NotNull BookingItemType itemType,
        @NotNull UUID refId,
        @Min(1) short quantity,
        LocalDate serviceDate,
        Map<String, Object> options
) {}
