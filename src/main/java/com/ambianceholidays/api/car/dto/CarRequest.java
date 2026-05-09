package com.ambianceholidays.api.car.dto;

import com.ambianceholidays.domain.car.CarCategory;
import com.ambianceholidays.domain.car.CarStatus;
import com.ambianceholidays.domain.car.CarUsageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CarRequest {

    @Data
    public static class ExtraServiceRequest {
        @NotBlank
        @Size(max = 100)
        private String name;
        @Min(0)
        private int priceCents;
    }


    @NotBlank
    @Size(max = 50)
    private String registrationNo;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private CarCategory category;

    @NotNull
    private CarUsageType usageType;

    @Min(1990)
    @Max(2100)
    private int year;

    @Min(1)
    @Max(30)
    private int passengerCapacity;

    private Integer luggageCapacity;
    private boolean hasAc = true;
    private boolean automatic = true;
    /** Gear count (5 / 6 / 8 etc.). Null when unknown. */
    @Min(1) @Max(12)
    private Short transmissionGears;
    private String fuelType = "Petrol";
    private String color;
    private String description;
    private String coverImageUrl;
    private List<String> galleryUrls = new ArrayList<>();
    private List<String> includes = new ArrayList<>();
    private List<String> excludes = new ArrayList<>();
    private UUID supplierId;

    @Valid
    private List<CarRateRequest> rates = new ArrayList<>();

    /** Optional add-on services bookable with this car. */
    @Valid
    private List<ExtraServiceRequest> extraServices = new ArrayList<>();

    /** Lifecycle state. Null defaults to ACTIVE on create; ignored on update. */
    private CarStatus status;
}
