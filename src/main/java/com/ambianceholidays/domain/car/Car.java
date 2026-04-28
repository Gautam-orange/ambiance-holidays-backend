package com.ambianceholidays.domain.car;

import com.ambianceholidays.domain.supplier.Supplier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cars")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "registration_no", nullable = false, unique = true, length = 50)
    private String registrationNo;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "car_category")
    private CarCategory category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "usage_type", nullable = false, columnDefinition = "car_usage_type")
    private CarUsageType usageType;

    @Column(nullable = false)
    private short year;

    @Column(name = "passenger_capacity", nullable = false)
    private short passengerCapacity;

    @Column(name = "luggage_capacity")
    private Short luggageCapacity;

    @Column(name = "has_ac", nullable = false)
    @Builder.Default
    private boolean hasAc = true;

    @Column(name = "is_automatic", nullable = false)
    @Builder.Default
    private boolean automatic = true;

    @Column(name = "fuel_type", length = 30)
    @Builder.Default
    private String fuelType = "Petrol";

    @Column(length = 50)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "gallery_urls", columnDefinition = "TEXT[]")
    private String[] galleryUrls;

    @Column(name = "includes", columnDefinition = "TEXT[]")
    private String[] includes;

    @Column(name = "excludes", columnDefinition = "TEXT[]")
    private String[] excludes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "car_status")
    @Builder.Default
    private CarStatus status = CarStatus.ACTIVE;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarRate> rates = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
