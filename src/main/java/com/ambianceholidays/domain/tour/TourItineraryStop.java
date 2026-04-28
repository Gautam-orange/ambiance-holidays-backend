package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tour_itinerary_stops")
public class TourItineraryStop {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "stop_time")
    private String stopTime;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder = 0;

    public UUID getId() { return id; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public String getStopTime() { return stopTime; }
    public void setStopTime(String stopTime) { this.stopTime = stopTime; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
}
