package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tour_pickup_zones")
public class TourPickupZone {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "extra_cents", nullable = false)
    private int extraCents = 0;

    @Column(name = "pickup_time")
    private LocalTime pickupTime;

    public UUID getId() { return id; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public int getExtraCents() { return extraCents; }
    public void setExtraCents(int extraCents) { this.extraCents = extraCents; }
    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime pickupTime) { this.pickupTime = pickupTime; }
}
