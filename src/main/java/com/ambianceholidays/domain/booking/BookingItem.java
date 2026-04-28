package com.ambianceholidays.domain.booking;

import com.ambianceholidays.domain.transfer.TransferTripType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "item_type", nullable = false)
    private BookingItemType itemType;

    @Column(name = "ref_id", nullable = false)
    private UUID refId;

    @Column(nullable = false)
    private short quantity = 1;

    @Column(name = "unit_price_cents", nullable = false)
    private int unitPriceCents;

    @Column(name = "total_cents", nullable = false)
    private int totalCents;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "pickup_location", columnDefinition = "TEXT")
    private String pickupLocation;

    @Column(name = "dropoff_location", columnDefinition = "TEXT")
    private String dropoffLocation;

    @Column(name = "pax_adults", nullable = false)
    private short paxAdults = 1;

    @Column(name = "pax_children", nullable = false)
    private short paxChildren = 0;

    @Column(name = "pax_infants", nullable = false)
    private short paxInfants = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trip_type")
    private TransferTripType tripType;

    @Column(name = "rental_days")
    private Short rentalDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "bookingItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingExtra> extras = new ArrayList<>();

    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public BookingItemType getItemType() { return itemType; }
    public void setItemType(BookingItemType itemType) { this.itemType = itemType; }
    public UUID getRefId() { return refId; }
    public void setRefId(UUID refId) { this.refId = refId; }
    public short getQuantity() { return quantity; }
    public void setQuantity(short quantity) { this.quantity = quantity; }
    public int getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(int unitPriceCents) { this.unitPriceCents = unitPriceCents; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }
    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    public short getPaxAdults() { return paxAdults; }
    public void setPaxAdults(short paxAdults) { this.paxAdults = paxAdults; }
    public short getPaxChildren() { return paxChildren; }
    public void setPaxChildren(short paxChildren) { this.paxChildren = paxChildren; }
    public short getPaxInfants() { return paxInfants; }
    public void setPaxInfants(short paxInfants) { this.paxInfants = paxInfants; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public TransferTripType getTripType() { return tripType; }
    public void setTripType(TransferTripType tripType) { this.tripType = tripType; }
    public Short getRentalDays() { return rentalDays; }
    public void setRentalDays(Short rentalDays) { this.rentalDays = rentalDays; }
    public Instant getCreatedAt() { return createdAt; }
    public List<BookingExtra> getExtras() { return extras; }
}
