package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TourResponse(
        UUID id,
        String title,
        String slug,
        String description,
        TourCategory category,
        TourRegion region,
        TourDuration duration,
        BigDecimal durationHours,
        int adultPriceCents,
        int childPriceCents,
        int infantPriceCents,
        short minPax,
        short maxPax,
        String[] includes,
        String[] excludes,
        String[] importantNotes,
        String coverImageUrl,
        String[] galleryUrls,
        TourStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<ItineraryStopResponse> itineraryStops,
        List<PickupZoneResponse> pickupZones
) {
    public record ItineraryStopResponse(UUID id, String stopTime, String title, String description, short sortOrder) {}
    public record PickupZoneResponse(UUID id, String zoneName, int extraCents, LocalTime pickupTime) {}

    public static TourResponse from(Tour t) {
        return new TourResponse(
                t.getId(), t.getTitle(), t.getSlug(), t.getDescription(),
                t.getCategory(), t.getRegion(), t.getDuration(), t.getDurationHours(),
                t.getAdultPriceCents(), t.getChildPriceCents(), t.getInfantPriceCents(),
                t.getMinPax(), t.getMaxPax(),
                t.getIncludes(), t.getExcludes(), t.getImportantNotes(),
                t.getCoverImageUrl(), t.getGalleryUrls(), t.getStatus(),
                t.getCreatedAt(), t.getUpdatedAt(),
                t.getItineraryStops().stream().map(s -> new ItineraryStopResponse(
                        s.getId(), s.getStopTime(), s.getTitle(), s.getDescription(), s.getSortOrder())).toList(),
                t.getPickupZones().stream().map(z -> new PickupZoneResponse(
                        z.getId(), z.getZoneName(), z.getExtraCents(), z.getPickupTime())).toList()
        );
    }
}
