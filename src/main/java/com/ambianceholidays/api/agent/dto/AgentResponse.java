package com.ambianceholidays.api.agent.dto;

import com.ambianceholidays.domain.agent.Agent;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentResponse {

    private UUID id;
    private String companyName;
    private String country;
    private String city;
    private String businessType;
    private String tier;
    private String status;
    private BigDecimal markupPercent;
    private BigDecimal commissionRate;
    private int creditLimit;
    private int totalBookings;
    private String businessProofUrl;
    private Instant approvedAt;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Instant createdAt;

    public static AgentResponse from(Agent a) {
        return AgentResponse.builder()
                .id(a.getId())
                .companyName(a.getCompanyName())
                .country(a.getCountry())
                .city(a.getCity())
                .businessType(a.getBusinessType().name())
                .tier(a.getTier().name())
                .status(a.getStatus().name())
                .markupPercent(a.getMarkupPercent())
                .commissionRate(a.getCommissionRate())
                .creditLimit(a.getCreditLimit())
                .totalBookings(a.getTotalBookings())
                .businessProofUrl(a.getBusinessProofUrl())
                .approvedAt(a.getApprovedAt())
                .email(a.getUser().getEmail())
                .firstName(a.getUser().getFirstName())
                .lastName(a.getUser().getLastName())
                .phone(a.getUser().getPhone())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
