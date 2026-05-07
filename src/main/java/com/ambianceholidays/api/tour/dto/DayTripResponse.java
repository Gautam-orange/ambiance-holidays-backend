package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DayTripResponse(
        UUID id,
        String title,
        String slug,
        String description,
        DayTripType tripType,
        TourRegion region,
        TourDuration duration,
        int adultPriceCents,
        int childPriceCents,
        int pricePerVehicleCents,
        int netRatePerPaxCents,
        BigDecimal markupPct,
        Short maxPax,
        String[] includes,
        String[] excludes,
        String coverImageUrl,
        String[] galleryUrls,
        String theme,
        String availabilityMode,
        TourStatus status,
        List<HighlightResponse> highlights,
        List<ItineraryStopResponse> itineraryStops,
        List<PickupZoneResponse> pickupZones,
        Instant createdAt,
        Instant updatedAt
) {
    public record HighlightResponse(UUID id, String text, short displayOrder) {}

    public record ItineraryStopResponse(
            UUID id,
            short stopOrder,
            String title,
            String timeLabel,
            String location,
            String description
    ) {}

    public record PickupZoneResponse(
            UUID id,
            String zoneName,
            String hotelName,
            String pickupFrom,
            String pickupTo,
            short sortOrder
    ) {}

    public static DayTripResponse from(DayTrip d) {
        List<HighlightResponse> hl = d.getHighlights() == null ? List.of()
                : d.getHighlights().stream()
                    .map(h -> new HighlightResponse(h.getId(), h.getText(), h.getDisplayOrder()))
                    .toList();
        List<ItineraryStopResponse> stops = d.getItineraryStops() == null ? List.of()
                : d.getItineraryStops().stream()
                    .map(s -> new ItineraryStopResponse(
                            s.getId(), s.getStopOrder(), s.getTitle(),
                            s.getTimeLabel(), s.getLocation(), s.getDescription()))
                    .toList();
        List<PickupZoneResponse> zones = d.getPickupZones() == null ? List.of()
                : d.getPickupZones().stream()
                    .map(z -> new PickupZoneResponse(
                            z.getId(), z.getZoneName(), z.getHotelName(),
                            z.getPickupFrom(), z.getPickupTo(), z.getSortOrder()))
                    .toList();
        return new DayTripResponse(
                d.getId(), d.getTitle(), d.getSlug(), d.getDescription(),
                d.getTripType(), d.getRegion(), d.getDuration(),
                d.getAdultPriceCents(), d.getChildPriceCents(),
                d.getPricePerVehicleCents(), d.getNetRatePerPaxCents(), d.getMarkupPct(),
                d.getMaxPax(),
                d.getIncludes(), d.getExcludes(), d.getCoverImageUrl(), d.getGalleryUrls(),
                d.getTheme(), d.getAvailabilityMode(),
                d.getStatus(),
                hl, stops, zones,
                d.getCreatedAt(), d.getUpdatedAt()
        );
    }
}
