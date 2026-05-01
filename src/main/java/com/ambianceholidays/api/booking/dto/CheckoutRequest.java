package com.ambianceholidays.api.booking.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CheckoutRequest(
        @NotBlank @Size(max = 100) String customerFirstName,
        @NotBlank @Size(max = 100) String customerLastName,
        @NotBlank @Email @Size(max = 254) String customerEmail,
        @Pattern(regexp = "\\+?[0-9 \\-()]{7,20}", message = "Invalid phone number")
        String customerPhone,
        // WhatsApp is normally a phone number too — accept the same regex.
        // Required because customers in Mauritius coordinate via WhatsApp.
        @NotBlank
        @Pattern(regexp = "\\+?[0-9 \\-()]{7,20}", message = "Invalid WhatsApp number")
        @Size(max = 30) String whatsappNumber,
        @NotBlank @Size(max = 100) String nationality,
        @NotBlank @Size(max = 500) String address,
        @NotNull LocalDate serviceDate,
        @Size(max = 1000) String specialRequests,
        BigDecimal markupPercent
) {}
