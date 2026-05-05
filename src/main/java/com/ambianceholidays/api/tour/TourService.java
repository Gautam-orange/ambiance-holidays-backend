package com.ambianceholidays.api.tour;

import com.ambianceholidays.api.tour.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.common.dto.PageMeta;
import com.ambianceholidays.domain.supplier.SupplierRepository;
import com.ambianceholidays.domain.tour.*;
import com.ambianceholidays.exception.BusinessException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class TourService {

    private final TourRepository tourRepo;
    private final DayTripRepository dayTripRepo;
    private final SupplierRepository supplierRepo;

    public TourService(TourRepository tourRepo, DayTripRepository dayTripRepo, SupplierRepository supplierRepo) {
        this.tourRepo = tourRepo;
        this.dayTripRepo = dayTripRepo;
        this.supplierRepo = supplierRepo;
    }

    // ── Tours ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<TourResponse>> listTours(String search, TourCategory category,
            TourRegion region, TourDuration duration, TourStatus status, int page, int size) {
        Specification<Tour> spec = buildTourSpec(search, category, region, duration, status);
        Page<Tour> pg = tourRepo.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(pg.getContent().stream().map(TourResponse::from).toList(),
                PageMeta.of(page, size, pg.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<TourResponse> getTour(UUID id) {
        Tour t = tourRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Tour"));
        return ApiResponse.ok(TourResponse.from(t));
    }

    @Transactional(readOnly = true)
    public ApiResponse<TourResponse> getTourBySlug(String slug) {
        Tour t = tourRepo.findBySlug(slug).orElseThrow(() -> BusinessException.notFound("Tour"));
        return ApiResponse.ok(TourResponse.from(t));
    }

    public ApiResponse<TourResponse> createTour(TourRequest req) {
        Tour tour = new Tour();
        applyTourRequest(tour, req);
        tour.setSlug(generateUniqueSlug(req.title()));
        tourRepo.save(tour);
        return ApiResponse.ok(TourResponse.from(tour));
    }

    public ApiResponse<TourResponse> updateTour(UUID id, TourRequest req) {
        Tour tour = tourRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Tour"));
        applyTourRequest(tour, req);
        tourRepo.save(tour);
        return ApiResponse.ok(TourResponse.from(tour));
    }

    public ApiResponse<Void> deleteTour(UUID id) {
        Tour tour = tourRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Tour"));
        tour.setDeletedAt(java.time.Instant.now());
        tourRepo.save(tour);
        return ApiResponse.ok(null);
    }

    private void applyTourRequest(Tour tour, TourRequest req) {
        tour.setTitle(req.title());
        tour.setDescription(req.description());
        tour.setCategory(req.category());
        tour.setRegion(req.region());
        tour.setDuration(req.duration());
        tour.setDurationHours(req.durationHours());
        tour.setAdultPriceCents(req.adultPriceCents());
        tour.setChildPriceCents(req.childPriceCents());
        tour.setInfantPriceCents(req.infantPriceCents());
        tour.setMinPax(req.minPax());
        tour.setMaxPax(req.maxPax());
        tour.setIncludes(req.includes());
        tour.setExcludes(req.excludes());
        tour.setImportantNotes(req.importantNotes());
        tour.setCoverImageUrl(req.coverImageUrl());
        tour.setGalleryUrls(req.galleryUrls());
        if (req.status() != null) tour.setStatus(req.status());
        if (req.supplierId() != null) {
            supplierRepo.findById(req.supplierId()).ifPresent(tour::setSupplier);
        }
        // sync itinerary stops
        tour.getItineraryStops().clear();
        if (req.itineraryStops() != null) {
            for (ItineraryStopRequest s : req.itineraryStops()) {
                TourItineraryStop stop = new TourItineraryStop();
                stop.setTour(tour);
                stop.setStopTime(s.stopTime());
                stop.setTitle(s.title());
                stop.setDescription(s.description());
                stop.setSortOrder((short) s.sortOrder());
                tour.getItineraryStops().add(stop);
            }
        }
        // sync pickup zones
        tour.getPickupZones().clear();
        if (req.pickupZones() != null) {
            for (PickupZoneRequest z : req.pickupZones()) {
                TourPickupZone zone = new TourPickupZone();
                zone.setTour(tour);
                zone.setZoneName(z.zoneName());
                zone.setExtraCents(z.extraCents());
                zone.setPickupTime(z.pickupTime());
                tour.getPickupZones().add(zone);
            }
        }
    }

    // ── Day Trips ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<DayTripResponse>> listDayTrips(String search, DayTripType tripType,
            TourRegion region, TourStatus status, int page, int size) {
        Specification<DayTrip> spec = buildDayTripSpec(search, tripType, region, status);
        Page<DayTrip> pg = dayTripRepo.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(pg.getContent().stream().map(DayTripResponse::from).toList(),
                PageMeta.of(page, size, pg.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<DayTripResponse> getDayTrip(UUID id) {
        DayTrip d = dayTripRepo.findById(id).orElseThrow(() -> BusinessException.notFound("DayTrip"));
        return ApiResponse.ok(DayTripResponse.from(d));
    }

    @Transactional(readOnly = true)
    public ApiResponse<DayTripResponse> getDayTripBySlug(String slug) {
        DayTrip d = dayTripRepo.findBySlug(slug).orElseThrow(() -> BusinessException.notFound("DayTrip"));
        return ApiResponse.ok(DayTripResponse.from(d));
    }

    public ApiResponse<DayTripResponse> createDayTrip(DayTripRequest req) {
        DayTrip d = new DayTrip();
        applyDayTripRequest(d, req);
        d.setSlug(generateSlug(req.title()) + "-" + UUID.randomUUID().toString().substring(0, 6));
        dayTripRepo.save(d);
        return ApiResponse.ok(DayTripResponse.from(d));
    }

    public ApiResponse<DayTripResponse> updateDayTrip(UUID id, DayTripRequest req) {
        DayTrip d = dayTripRepo.findById(id).orElseThrow(() -> BusinessException.notFound("DayTrip"));
        applyDayTripRequest(d, req);
        dayTripRepo.save(d);
        return ApiResponse.ok(DayTripResponse.from(d));
    }

    public ApiResponse<DayTripResponse> updateDayTripStatus(UUID id, TourStatus status) {
        DayTrip d = dayTripRepo.findById(id).orElseThrow(() -> BusinessException.notFound("DayTrip"));
        d.setStatus(status);
        dayTripRepo.save(d);
        return ApiResponse.ok(DayTripResponse.from(d));
    }

    public ApiResponse<Void> deleteDayTrip(UUID id) {
        DayTrip d = dayTripRepo.findById(id).orElseThrow(() -> BusinessException.notFound("DayTrip"));
        d.setDeletedAt(java.time.Instant.now());
        dayTripRepo.save(d);
        return ApiResponse.ok(null);
    }

    private void applyDayTripRequest(DayTrip d, DayTripRequest req) {
        d.setTitle(req.title());
        d.setDescription(req.description());
        d.setTripType(req.tripType());
        d.setRegion(req.region());
        d.setDuration(req.duration());
        d.setAdultPriceCents(req.adultPriceCents());
        d.setChildPriceCents(req.childPriceCents());
        d.setMaxPax(req.maxPax());
        d.setIncludes(req.includes());
        d.setExcludes(req.excludes());
        d.setCoverImageUrl(req.coverImageUrl());
        d.setGalleryUrls(req.galleryUrls());
        if (req.status() != null) d.setStatus(req.status());
        if (req.supplierId() != null) {
            supplierRepo.findById(req.supplierId()).ifPresent(d::setSupplier);
        }
    }

    // ── Specs & Slugs ────────────────────────────────────────────────────────

    private Specification<Tour> buildTourSpec(String search, TourCategory category,
            TourRegion region, TourDuration duration, TourStatus status) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }
            if (category != null) preds.add(cb.equal(root.get("category"), category));
            if (region != null) preds.add(cb.equal(root.get("region"), region));
            if (duration != null) preds.add(cb.equal(root.get("duration"), duration));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private Specification<DayTrip> buildDayTripSpec(String search, DayTripType tripType,
            TourRegion region, TourStatus status) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }
            if (tripType != null) preds.add(cb.equal(root.get("tripType"), tripType));
            if (region != null) preds.add(cb.equal(root.get("region"), region));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private String generateUniqueSlug(String title) {
        String base = generateSlug(title);
        if (!tourRepo.existsBySlug(base)) return base;
        return base + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private static final Pattern NON_ALPHA = Pattern.compile("[^a-z0-9]+");

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title.toLowerCase(), Normalizer.Form.NFD);
        return NON_ALPHA.matcher(normalized).replaceAll("-").replaceAll("^-|-$", "");
    }
}
