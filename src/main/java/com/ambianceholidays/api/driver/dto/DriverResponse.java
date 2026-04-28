package com.ambianceholidays.api.driver.dto;

import com.ambianceholidays.domain.driver.Driver;
import com.ambianceholidays.domain.driver.DriverStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DriverResponse {
    private UUID id;
    private String code;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String licenseNo;
    private LocalDate licenseExpiry;
    private int experienceYears;
    private DriverStatus status;
    private String photoUrl;
    private boolean active;

    public static DriverResponse from(Driver d) {
        DriverResponse r = new DriverResponse();
        r.id = d.getId();
        r.code = d.getCode();
        r.firstName = d.getFirstName();
        r.lastName = d.getLastName();
        r.fullName = d.getFullName();
        r.phone = d.getPhone();
        r.email = d.getEmail();
        r.address = d.getAddress();
        r.licenseNo = d.getLicenseNo();
        r.licenseExpiry = d.getLicenseExpiry();
        r.experienceYears = d.getExperienceYears();
        r.status = d.getStatus();
        r.photoUrl = d.getPhotoUrl();
        r.active = d.isActive();
        return r;
    }
}
