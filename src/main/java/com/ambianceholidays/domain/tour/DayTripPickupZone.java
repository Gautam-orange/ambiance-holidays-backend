package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "day_trip_pickup_zones")
public class DayTripPickupZone {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_trip_id", nullable = false)
    private DayTrip dayTrip;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "hotel_name")
    private String hotelName;

    @Column(name = "pickup_from")
    private String pickupFrom;

    @Column(name = "pickup_to")
    private String pickupTo;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder = 0;

    public UUID getId() { return id; }
    public DayTrip getDayTrip() { return dayTrip; }
    public void setDayTrip(DayTrip dayTrip) { this.dayTrip = dayTrip; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getPickupFrom() { return pickupFrom; }
    public void setPickupFrom(String pickupFrom) { this.pickupFrom = pickupFrom; }
    public String getPickupTo() { return pickupTo; }
    public void setPickupTo(String pickupTo) { this.pickupTo = pickupTo; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
}
