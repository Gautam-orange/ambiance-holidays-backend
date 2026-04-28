package com.ambianceholidays.api.transfer.dto;

import com.ambianceholidays.domain.car.CarCategory;
import com.ambianceholidays.domain.transfer.TransferRoute;
import com.ambianceholidays.domain.transfer.TransferTripType;

import java.util.UUID;

public record TransferRouteResponse(
        UUID id,
        String fromLocation,
        String toLocation,
        TransferTripType tripType,
        CarCategory carCategory,
        int basePriceCents,
        Short estDurationMins,
        Short estKm,
        boolean active
) {
    public static TransferRouteResponse from(TransferRoute r) {
        return new TransferRouteResponse(
                r.getId(), r.getFromLocation(), r.getToLocation(),
                r.getTripType(), r.getCarCategory(), r.getBasePriceCents(),
                r.getEstDurationMins(), r.getEstKm(), r.isActive()
        );
    }
}
