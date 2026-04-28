package com.ambianceholidays.domain.booking;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "booking_extras")
public class BookingExtra {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_item_id", nullable = false)
    private BookingItem bookingItem;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private short quantity = 1;

    @Column(name = "unit_price_cents", nullable = false)
    private int unitPriceCents;

    @Column(name = "total_cents", nullable = false)
    private int totalCents;

    public UUID getId() { return id; }
    public BookingItem getBookingItem() { return bookingItem; }
    public void setBookingItem(BookingItem bookingItem) { this.bookingItem = bookingItem; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public short getQuantity() { return quantity; }
    public void setQuantity(short quantity) { this.quantity = quantity; }
    public int getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(int unitPriceCents) { this.unitPriceCents = unitPriceCents; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
}
