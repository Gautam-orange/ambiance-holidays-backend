package com.ambianceholidays.api.driver.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DriverRequest {
    private String code;

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;

    @NotBlank @Size(max = 30)
    private String phone;

    @Email
    private String email;

    private String address;

    @NotBlank @Size(max = 100)
    private String licenseNo;

    @NotNull
    private LocalDate licenseExpiry;

    @Min(0) @Max(60)
    private int experienceYears;

    private String photoUrl;
}
