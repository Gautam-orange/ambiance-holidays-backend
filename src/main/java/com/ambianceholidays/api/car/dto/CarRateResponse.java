package com.ambianceholidays.api.car.dto;

import com.ambianceholidays.domain.car.CarRate;
import com.ambianceholidays.domain.car.RatePeriod;
import lombok.Data;

import java.util.UUID;

@Data
public class CarRateResponse {
    private UUID id;
    private RatePeriod period;
    private int amountCents;
    private Integer kmFrom;
    private Integer kmTo;

    public static CarRateResponse from(CarRate rate) {
        CarRateResponse r = new CarRateResponse();
        r.id = rate.getId();
        r.period = rate.getPeriod();
        r.amountCents = rate.getAmountCents();
        r.kmFrom = rate.getKmFrom();
        r.kmTo = rate.getKmTo();
        return r;
    }
}
