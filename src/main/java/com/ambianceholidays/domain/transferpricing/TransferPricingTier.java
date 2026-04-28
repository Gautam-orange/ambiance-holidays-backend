package com.ambianceholidays.domain.transferpricing;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_pricing_tiers")
public class TransferPricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String label;

    @Column(name = "min_km", nullable = false)
    private int minKm;

    @Column(name = "max_km")
    private Integer maxKm; // null = unlimited

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // --- Getters & Setters ---
    public UUID getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getMinKm() { return minKm; }
    public void setMinKm(int minKm) { this.minKm = minKm; }
    public Integer getMaxKm() { return maxKm; }
    public void setMaxKm(Integer maxKm) { this.maxKm = maxKm; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
