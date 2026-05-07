package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "day_trip_highlights")
public class DayTripHighlight {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_trip_id", nullable = false)
    private DayTrip dayTrip;

    @Column(nullable = false)
    private String text;

    @Column(name = "display_order", nullable = false)
    private short displayOrder = 0;

    public UUID getId() { return id; }
    public DayTrip getDayTrip() { return dayTrip; }
    public void setDayTrip(DayTrip dayTrip) { this.dayTrip = dayTrip; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public short getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(short displayOrder) { this.displayOrder = displayOrder; }
}
