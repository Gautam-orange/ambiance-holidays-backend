package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import com.ambianceholidays.domain.supplier.Supplier;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "day_trips")
@SQLRestriction("deleted_at IS NULL")
public class DayTrip {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trip_type", nullable = false)
    private DayTripType tripType = DayTripType.SHARED;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourRegion region;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourDuration duration;

    @Column(name = "adult_price_cents", nullable = false)
    private int adultPriceCents;

    @Column(name = "child_price_cents", nullable = false)
    private int childPriceCents;

    @Column(name = "max_pax")
    private Short maxPax;

    @Column(columnDefinition = "TEXT[]")
    private String[] includes;

    @Column(columnDefinition = "TEXT[]")
    private String[] excludes;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "gallery_urls", columnDefinition = "TEXT[]")
    private String[] galleryUrls;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourStatus status = TourStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DayTripType getTripType() { return tripType; }
    public void setTripType(DayTripType tripType) { this.tripType = tripType; }
    public TourRegion getRegion() { return region; }
    public void setRegion(TourRegion region) { this.region = region; }
    public TourDuration getDuration() { return duration; }
    public void setDuration(TourDuration duration) { this.duration = duration; }
    public int getAdultPriceCents() { return adultPriceCents; }
    public void setAdultPriceCents(int adultPriceCents) { this.adultPriceCents = adultPriceCents; }
    public int getChildPriceCents() { return childPriceCents; }
    public void setChildPriceCents(int childPriceCents) { this.childPriceCents = childPriceCents; }
    public Short getMaxPax() { return maxPax; }
    public void setMaxPax(Short maxPax) { this.maxPax = maxPax; }
    public String[] getIncludes() { return includes; }
    public void setIncludes(String[] includes) { this.includes = includes; }
    public String[] getExcludes() { return excludes; }
    public void setExcludes(String[] excludes) { this.excludes = excludes; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String[] getGalleryUrls() { return galleryUrls; }
    public void setGalleryUrls(String[] galleryUrls) { this.galleryUrls = galleryUrls; }
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
