package com.ambianceholidays.domain.transfer;

import com.ambianceholidays.domain.car.CarCategory;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_routes")
public class TransferRoute {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "from_location", nullable = false)
    private String fromLocation;

    @Column(name = "to_location", nullable = false)
    private String toLocation;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trip_type", nullable = false)
    private TransferTripType tripType = TransferTripType.ONE_WAY;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "car_category", nullable = false)
    private CarCategory carCategory;

    @Column(name = "base_price_cents", nullable = false)
    private int basePriceCents;

    @Column(name = "est_duration_mins")
    private Short estDurationMins;

    @Column(name = "est_km")
    private Short estKm;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public TransferTripType getTripType() { return tripType; }
    public void setTripType(TransferTripType tripType) { this.tripType = tripType; }
    public CarCategory getCarCategory() { return carCategory; }
    public void setCarCategory(CarCategory carCategory) { this.carCategory = carCategory; }
    public int getBasePriceCents() { return basePriceCents; }
    public void setBasePriceCents(int basePriceCents) { this.basePriceCents = basePriceCents; }
    public Short getEstDurationMins() { return estDurationMins; }
    public void setEstDurationMins(Short estDurationMins) { this.estDurationMins = estDurationMins; }
    public Short getEstKm() { return estKm; }
    public void setEstKm(Short estKm) { this.estKm = estKm; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
