package com.ambianceholidays.api.admin;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.agent.AgentRepository;
import com.ambianceholidays.domain.agent.AgentStatus;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingRepository;
import com.ambianceholidays.domain.booking.BookingStatus;
import com.ambianceholidays.domain.car.CarRepository;
import com.ambianceholidays.domain.car.CarStatus;
import com.ambianceholidays.domain.tour.DayTripRepository;
import com.ambianceholidays.domain.tour.TourRepository;
import com.ambianceholidays.domain.tour.TourStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final BookingRepository bookingRepo;
    private final AgentRepository agentRepo;
    private final CarRepository carRepo;
    private final TourRepository tourRepo;
    private final DayTripRepository dayTripRepo;

    public AdminDashboardController(BookingRepository bookingRepo, AgentRepository agentRepo,
            CarRepository carRepo, TourRepository tourRepo, DayTripRepository dayTripRepo) {
        this.bookingRepo = bookingRepo;
        this.agentRepo = agentRepo;
        this.carRepo = carRepo;
        this.tourRepo = tourRepo;
        this.dayTripRepo = dayTripRepo;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<Map<String, Object>> stats() {
        Instant now = Instant.now();
        Instant monthStart = now.truncatedTo(ChronoUnit.DAYS).minus(30, ChronoUnit.DAYS);

        // Single read of non-deleted bookings drives every counter / revenue figure below
        // so they stay consistent (the previous code used unfiltered count() for the total
        // which silently included soft-deleted rows).
        List<Booking> allBookings = bookingRepo.findAll(
                (Specification<Booking>) (root, query, cb) ->
                        cb.isNull(root.get("deletedAt")));

        long totalBookings = allBookings.size();
        long pendingBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long confirmedBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelledBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        long revenueTotal = allBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .mapToLong(Booking::getTotalCents)
                .sum();

        long revenueThisMonth = allBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(monthStart))
                .mapToLong(Booking::getTotalCents)
                .sum();

        // Agents
        long totalAgents = agentRepo.findAll().stream().filter(a -> a.getDeletedAt() == null).count();
        long pendingAgents = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.PENDING);
        long activeAgents = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.ACTIVE);

        // Assets
        long activeCars = carRepo.findAll().stream()
                .filter(c -> c.getDeletedAt() == null && c.getStatus() == CarStatus.ACTIVE)
                .count();
        long activeTours = tourRepo.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getStatus() == TourStatus.ACTIVE)
                .count();
        long activeDayTrips = dayTripRepo.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getStatus() == TourStatus.ACTIVE)
                .count();

        return ApiResponse.ok(Map.of(
                "bookings", Map.of(
                        "total", totalBookings,
                        "pending", pendingBookings,
                        "confirmed", confirmedBookings,
                        "cancelled", cancelledBookings),
                "revenue", Map.of(
                        "total", revenueTotal,
                        "thisMonth", revenueThisMonth),
                "agents", Map.of(
                        "total", totalAgents,
                        "pending", pendingAgents,
                        "active", activeAgents),
                "assets", Map.of(
                        "activeCars", activeCars,
                        "activeTours", activeTours,
                        "activeDayTrips", activeDayTrips)));
    }

}
