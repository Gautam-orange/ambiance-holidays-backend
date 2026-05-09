package com.ambianceholidays.domain.car;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Optional add-on services bookable alongside a car rental (Baby Seat, GPS,
 * Additional Driver, etc.). Created in V3 (`car_extra_services`) but historically
 * encoded as `XSVC:Name:PriceCents` strings inside `cars.includes` — V14
 * migrates those rows back into this table and strips the encoded entries.
 */
@Entity
@Table(name = "car_extra_services")
public class CarExtraService {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private int priceCents = 0;

    @Column(name = "display_order", nullable = false)
    private short displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public short getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(short displayOrder) { this.displayOrder = displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
}
