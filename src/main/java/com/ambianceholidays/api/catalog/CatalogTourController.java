package com.ambianceholidays.api.catalog;

import com.ambianceholidays.api.tour.TourService;
import com.ambianceholidays.api.tour.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.tour.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogTourController {

    private final TourService tourService;

    public CatalogTourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping("/tours")
    public ApiResponse<List<TourResponse>> listTours(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TourCategory category,
            @RequestParam(required = false) TourRegion region,
            @RequestParam(required = false) TourDuration duration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return tourService.listTours(search, category, region, duration, TourStatus.ACTIVE, page, size);
    }

    @GetMapping("/tours/{slug}")
    public ApiResponse<TourResponse> getTour(@PathVariable String slug) {
        return tourService.getTourBySlug(slug);
    }

    @GetMapping("/day-trips")
    public ApiResponse<List<DayTripResponse>> listDayTrips(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DayTripType tripType,
            @RequestParam(required = false) TourRegion region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return tourService.listDayTrips(search, tripType, region, TourStatus.ACTIVE, page, size);
    }

    @GetMapping("/day-trips/{slug}")
    public ApiResponse<DayTripResponse> getDayTrip(@PathVariable String slug) {
        return tourService.getDayTripBySlug(slug);
    }
}
