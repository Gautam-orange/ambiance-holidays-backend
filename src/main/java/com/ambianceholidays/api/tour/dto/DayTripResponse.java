package com.ambianceholidays.api.tour.dto;

import com.ambianceholidays.domain.tour.*;

import java.time.Instant;
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
        Short maxPax,
        String[] includes,
        String[] excludes,
        String coverImageUrl,
        String[] galleryUrls,
        TourStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static DayTripResponse from(DayTrip d) {
        return new DayTripResponse(
                d.getId(), d.getTitle(), d.getSlug(), d.getDescription(),
                d.getTripType(), d.getRegion(), d.getDuration(),
                d.getAdultPriceCents(), d.getChildPriceCents(), d.getMaxPax(),
                d.getIncludes(), d.getExcludes(), d.getCoverImageUrl(), d.getGalleryUrls(),
                d.getStatus(), d.getCreatedAt(), d.getUpdatedAt()
        );
    }
}
