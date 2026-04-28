package com.ambianceholidays.domain.tour;

import com.ambianceholidays.domain.supplier.Supplier;
import com.ambianceholidays.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tours")
@SQLRestriction("deleted_at IS NULL")
public class Tour {

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
    @Column(nullable = false)
    private TourCategory category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourRegion region;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourDuration duration;

    @Column(name = "duration_hours", precision = 4, scale = 1)
    private BigDecimal durationHours;

    @Column(name = "adult_price_cents", nullable = false)
    private int adultPriceCents;

    @Column(name = "child_price_cents", nullable = false)
    private int childPriceCents;

    @Column(name = "infant_price_cents", nullable = false)
    private int infantPriceCents = 0;

    @Column(name = "min_pax", nullable = false)
    private short minPax = 1;

    @Column(name = "max_pax", nullable = false)
    private short maxPax = 20;

    @Column(columnDefinition = "TEXT[]")
    private String[] includes;

    @Column(columnDefinition = "TEXT[]")
    private String[] excludes;

    @Column(name = "important_notes", columnDefinition = "TEXT[]")
    private String[] importantNotes;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "gallery_urls", columnDefinition = "TEXT[]")
    private String[] galleryUrls;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TourStatus status = TourStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TourItineraryStop> itineraryStops = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourPickupZone> pickupZones = new ArrayList<>();

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
    public TourCategory getCategory() { return category; }
    public void setCategory(TourCategory category) { this.category = category; }
    public TourRegion getRegion() { return region; }
    public void setRegion(TourRegion region) { this.region = region; }
    public TourDuration getDuration() { return duration; }
    public void setDuration(TourDuration duration) { this.duration = duration; }
    public BigDecimal getDurationHours() { return durationHours; }
    public void setDurationHours(BigDecimal durationHours) { this.durationHours = durationHours; }
    public int getAdultPriceCents() { return adultPriceCents; }
    public void setAdultPriceCents(int adultPriceCents) { this.adultPriceCents = adultPriceCents; }
    public int getChildPriceCents() { return childPriceCents; }
    public void setChildPriceCents(int childPriceCents) { this.childPriceCents = childPriceCents; }
    public int getInfantPriceCents() { return infantPriceCents; }
    public void setInfantPriceCents(int infantPriceCents) { this.infantPriceCents = infantPriceCents; }
    public short getMinPax() { return minPax; }
    public void setMinPax(short minPax) { this.minPax = minPax; }
    public short getMaxPax() { return maxPax; }
    public void setMaxPax(short maxPax) { this.maxPax = maxPax; }
    public String[] getIncludes() { return includes; }
    public void setIncludes(String[] includes) { this.includes = includes; }
    public String[] getExcludes() { return excludes; }
    public void setExcludes(String[] excludes) { this.excludes = excludes; }
    public String[] getImportantNotes() { return importantNotes; }
    public void setImportantNotes(String[] importantNotes) { this.importantNotes = importantNotes; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String[] getGalleryUrls() { return galleryUrls; }
    public void setGalleryUrls(String[] galleryUrls) { this.galleryUrls = galleryUrls; }
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public List<TourItineraryStop> getItineraryStops() { return itineraryStops; }
    public List<TourPickupZone> getPickupZones() { return pickupZones; }
}
