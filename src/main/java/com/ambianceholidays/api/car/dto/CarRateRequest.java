package com.ambianceholidays.api.car.dto;

import com.ambianceholidays.domain.car.RatePeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarRateRequest {
    @NotNull
    private RatePeriod period;

    @NotNull
    @Min(0)
    private Integer amountCents;

    private Integer kmFrom;
    private Integer kmTo;
}
