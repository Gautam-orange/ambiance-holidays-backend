package com.ambianceholidays.api.car.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BlockDatesRequest {
    @NotNull
    private LocalDate dateFrom;
    @NotNull
    private LocalDate dateTo;
    private String reason = "BLOCKED";
}
