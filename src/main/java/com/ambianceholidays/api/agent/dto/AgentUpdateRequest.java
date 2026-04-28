package com.ambianceholidays.api.agent.dto;

import com.ambianceholidays.domain.agent.AgentTier;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record AgentUpdateRequest(
        AgentTier tier,
        @DecimalMin("0") @DecimalMax("100") BigDecimal markupPercent,
        @DecimalMin("0") @DecimalMax("100") BigDecimal commissionRate,
        @Min(0) Integer creditLimit
) {}
