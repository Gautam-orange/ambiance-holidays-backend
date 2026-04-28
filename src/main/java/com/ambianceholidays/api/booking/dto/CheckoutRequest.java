package com.ambianceholidays.api.booking.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CheckoutRequest(
        @NotBlank String customerFirstName,
        @NotBlank String customerLastName,
        @NotBlank @Email String customerEmail,
        @Pattern(regexp = "\\+?[0-9 \\-()]{7,20}") String customerPhone,
        @NotNull LocalDate serviceDate,
        String specialRequests,
        BigDecimal markupPercent
) {}
