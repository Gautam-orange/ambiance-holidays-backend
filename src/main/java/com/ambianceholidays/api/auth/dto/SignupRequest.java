package com.ambianceholidays.api.auth.dto;

import com.ambianceholidays.domain.agent.BusinessType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase, lowercase, digit and special character"
    )
    private String password;

    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String companyName;

    @NotBlank(message = "Country is required")
    private String country;

    private String city;
    private String address;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    private String phone;
    private String whatsapp;
}
