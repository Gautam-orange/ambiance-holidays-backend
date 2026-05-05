package com.ambianceholidays.api.tour;

import com.ambianceholidays.api.tour.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.tour.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminTourController {

    private final TourService tourService;

    public AdminTourController(TourService tourService) {
        this.tourService = tourService;
    }

    // ── Tours ─────────────────────────────────────────────────────────────────

    @GetMapping("/tours")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<TourResponse>> listTours(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TourCategory category,
            @RequestParam(required = false) TourRegion region,
            @RequestParam(required = false) TourDuration duration,
            @RequestParam(required = false) TourStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return tourService.listTours(search, category, region, duration, status, page, size);
    }

    @GetMapping("/tours/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<TourResponse> getTour(@PathVariable UUID id) {
        return tourService.getTour(id);
    }

    @PostMapping("/tours")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TourResponse> createTour(@Valid @RequestBody TourRequest req) {
        return tourService.createTour(req);
    }

    @PutMapping("/tours/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TourResponse> updateTour(@PathVariable UUID id, @Valid @RequestBody TourRequest req) {
        return tourService.updateTour(id, req);
    }

    @DeleteMapping("/tours/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<Void> deleteTour(@PathVariable UUID id) {
        return tourService.deleteTour(id);
    }

    // ── Day Trips ─────────────────────────────────────────────────────────────

    @GetMapping("/day-trips")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<DayTripResponse>> listDayTrips(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DayTripType tripType,
            @RequestParam(required = false) TourRegion region,
            @RequestParam(required = false) TourStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return tourService.listDayTrips(search, tripType, region, status, page, size);
    }

    @GetMapping("/day-trips/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<DayTripResponse> getDayTrip(@PathVariable UUID id) {
        return tourService.getDayTrip(id);
    }

    @PostMapping("/day-trips")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<DayTripResponse> createDayTrip(@Valid @RequestBody DayTripRequest req) {
        return tourService.createDayTrip(req);
    }

    @PutMapping("/day-trips/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<DayTripResponse> updateDayTrip(@PathVariable UUID id, @Valid @RequestBody DayTripRequest req) {
        return tourService.updateDayTrip(id, req);
    }

    @DeleteMapping("/day-trips/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<Void> deleteDayTrip(@PathVariable UUID id) {
        return tourService.deleteDayTrip(id);
    }

    /** Toggle a day-trip's ACTIVE/INACTIVE/ON_REQUEST status without resending the full payload. */
    @PatchMapping("/day-trips/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<DayTripResponse> updateDayTripStatus(@PathVariable UUID id,
            @RequestBody java.util.Map<String, String> body) {
        String s = body == null ? null : body.get("status");
        TourStatus status;
        try { status = TourStatus.valueOf(s); }
        catch (Exception e) { throw com.ambianceholidays.exception.BusinessException.badRequest("INVALID_STATUS", "Invalid status: " + s); }
        return tourService.updateDayTripStatus(id, status);
    }
}
