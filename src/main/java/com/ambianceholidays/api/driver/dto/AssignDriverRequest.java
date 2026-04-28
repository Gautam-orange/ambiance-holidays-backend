package com.ambianceholidays.api.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AssignDriverRequest {
    @NotNull
    private UUID driverId;
    @NotNull
    private UUID carId;
    @NotNull
    private Instant startAt;
    @NotNull
    private Instant endAt;
    private String pickupAddress;
    private String dropoffAddress;
    private String notes;
}
