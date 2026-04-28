package com.ambianceholidays.api.car.dto;

import com.ambianceholidays.domain.car.*;
import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
public class CarResponse {
    private UUID id;
    private String registrationNo;
    private String name;
    private CarCategory category;
    private CarUsageType usageType;
    private int year;
    private int passengerCapacity;
    private Integer luggageCapacity;
    private boolean hasAc;
    private boolean automatic;
    private String fuelType;
    private String color;
    private String description;
    private String coverImageUrl;
    private List<String> galleryUrls;
    private List<String> includes;
    private List<String> excludes;
    private CarStatus status;
    private String supplierName;
    private UUID supplierId;
    private List<CarRateResponse> rates;
    private Instant createdAt;

    public static CarResponse from(Car car, List<CarRate> rates) {
        CarResponse r = new CarResponse();
        r.id = car.getId();
        r.registrationNo = car.getRegistrationNo();
        r.name = car.getName();
        r.category = car.getCategory();
        r.usageType = car.getUsageType();
        r.year = car.getYear();
        r.passengerCapacity = car.getPassengerCapacity();
        r.luggageCapacity = car.getLuggageCapacity() != null ? car.getLuggageCapacity().intValue() : null;
        r.hasAc = car.isHasAc();
        r.automatic = car.isAutomatic();
        r.fuelType = car.getFuelType();
        r.color = car.getColor();
        r.description = car.getDescription();
        r.coverImageUrl = car.getCoverImageUrl();
        r.galleryUrls = car.getGalleryUrls() != null ? Arrays.asList(car.getGalleryUrls()) : Collections.emptyList();
        r.includes = car.getIncludes() != null ? Arrays.asList(car.getIncludes()) : Collections.emptyList();
        r.excludes = car.getExcludes() != null ? Arrays.asList(car.getExcludes()) : Collections.emptyList();
        r.status = car.getStatus();
        r.supplierId = car.getSupplier() != null ? car.getSupplier().getId() : null;
        r.supplierName = car.getSupplier() != null ? car.getSupplier().getName() : null;
        r.rates = rates.stream().map(CarRateResponse::from).toList();
        r.createdAt = car.getCreatedAt();
        return r;
    }
}
