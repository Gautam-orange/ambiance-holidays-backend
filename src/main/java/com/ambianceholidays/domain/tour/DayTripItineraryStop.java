package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "day_trip_itinerary_stops")
public class DayTripItineraryStop {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_trip_id", nullable = false)
    private DayTrip dayTrip;

    @Column(name = "stop_order", nullable = false)
    private short stopOrder = 0;

    @Column(nullable = false)
    private String title;

    @Column(name = "time_label")
    private String timeLabel;

    @Column
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    public UUID getId() { return id; }
    public DayTrip getDayTrip() { return dayTrip; }
    public void setDayTrip(DayTrip dayTrip) { this.dayTrip = dayTrip; }
    public short getStopOrder() { return stopOrder; }
    public void setStopOrder(short stopOrder) { this.stopOrder = stopOrder; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
