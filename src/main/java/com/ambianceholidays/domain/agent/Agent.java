package com.ambianceholidays.domain.agent;

import com.ambianceholidays.common.domain.BaseEntity;
import com.ambianceholidays.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "business_type", columnDefinition = "business_type", nullable = false)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "agent_tier", nullable = false)
    @Builder.Default
    private AgentTier tier = AgentTier.BRONZE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "agent_status", nullable = false)
    @Builder.Default
    private AgentStatus status = AgentStatus.PENDING;

    @Column(name = "markup_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal markupPercent = BigDecimal.ZERO;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("10.00");

    @Column(name = "credit_limit", nullable = false)
    @Builder.Default
    private Integer creditLimit = 0;

    @Column(name = "total_bookings", nullable = false)
    @Builder.Default
    private Integer totalBookings = 0;

    @Column(name = "business_proof_url", length = 500)
    private String businessProofUrl;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    public boolean isActive() {
        return status == AgentStatus.ACTIVE;
    }

    public boolean isPending() {
        return status == AgentStatus.PENDING;
    }
}
