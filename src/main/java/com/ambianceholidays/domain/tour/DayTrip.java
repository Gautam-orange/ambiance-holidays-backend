package com.ambianceholidays.domain.tour;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import com.ambianceholidays.domain.supplier.Supplier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "price_per_vehicle_cents", nullable = false)
    private int pricePerVehicleCents = 0;

    @Column(name = "net_rate_per_pax_cents", nullable = false)
    private int netRatePerPaxCents = 0;

    @Column(name = "markup_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal markupPct = BigDecimal.ZERO;

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

    /** NATURE / ADVENTURE / CULTURAL / SEA_ACTIVITIES / BEACH — see V3 chk constraint. */
    @Column(length = 30)
    private String theme;

    /** "always" or "on_request" — separate from status to allow ACTIVE+on_request. */
    @Column(name = "availability_mode", length = 20, nullable = false)
    private String availabilityMode = "always";

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

    @OneToMany(mappedBy = "dayTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<DayTripItineraryStop> itineraryStops = new ArrayList<>();

    @OneToMany(mappedBy = "dayTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<DayTripPickupZone> pickupZones = new ArrayList<>();

    @OneToMany(mappedBy = "dayTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<DayTripHighlight> highlights = new ArrayList<>();

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
    public int getPricePerVehicleCents() { return pricePerVehicleCents; }
    public void setPricePerVehicleCents(int pricePerVehicleCents) { this.pricePerVehicleCents = pricePerVehicleCents; }
    public int getNetRatePerPaxCents() { return netRatePerPaxCents; }
    public void setNetRatePerPaxCents(int netRatePerPaxCents) { this.netRatePerPaxCents = netRatePerPaxCents; }
    public BigDecimal getMarkupPct() { return markupPct; }
    public void setMarkupPct(BigDecimal markupPct) { this.markupPct = markupPct == null ? BigDecimal.ZERO : markupPct; }
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
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getAvailabilityMode() { return availabilityMode; }
    public void setAvailabilityMode(String availabilityMode) {
        this.availabilityMode = (availabilityMode == null || availabilityMode.isBlank()) ? "always" : availabilityMode;
    }
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public List<DayTripItineraryStop> getItineraryStops() { return itineraryStops; }
    public List<DayTripPickupZone> getPickupZones() { return pickupZones; }
    public List<DayTripHighlight> getHighlights() { return highlights; }
}
