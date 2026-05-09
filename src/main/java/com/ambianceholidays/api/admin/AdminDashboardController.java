package com.ambianceholidays.api.admin;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.agent.AgentRepository;
import com.ambianceholidays.domain.agent.AgentStatus;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingItem;
import com.ambianceholidays.domain.booking.BookingItemType;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

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

        // Per-module breakdown (current bookings = serviceDate ≥ today AND not cancelled;
        // upcoming scheduled = serviceDate strictly in the future). Revenue per module is
        // summed by line-item totalCents on non-cancelled bookings.
        Map<BookingItemType, Long> currentByType = new HashMap<>();
        Map<BookingItemType, Long> upcomingByType = new HashMap<>();
        Map<BookingItemType, Long> revenueByType = new HashMap<>();
        Map<BookingItemType, Long> revenueThisMonthByType = new HashMap<>();
        for (BookingItemType t : BookingItemType.values()) {
            currentByType.put(t, 0L);
            upcomingByType.put(t, 0L);
            revenueByType.put(t, 0L);
            revenueThisMonthByType.put(t, 0L);
        }
        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) continue;
            boolean isCurrent = b.getServiceDate() != null && !b.getServiceDate().isBefore(today);
            boolean isUpcoming = b.getServiceDate() != null && b.getServiceDate().isAfter(today);
            boolean inMonth = b.getCreatedAt() != null && b.getCreatedAt().isAfter(monthStart);
            for (BookingItem item : b.getItems()) {
                BookingItemType t = item.getItemType();
                if (t == null) continue;
                if (isCurrent) currentByType.merge(t, 1L, Long::sum);
                if (isUpcoming) upcomingByType.merge(t, 1L, Long::sum);
                revenueByType.merge(t, (long) item.getTotalCents(), Long::sum);
                if (inMonth) revenueThisMonthByType.merge(t, (long) item.getTotalCents(), Long::sum);
            }
        }

        // Agents
        long totalAgents = agentRepo.findAll().stream().filter(a -> a.getDeletedAt() == null).count();
        long pendingAgents = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.PENDING);
        long activeAgents = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.ACTIVE);

        // Assets — Active + Inactive per module
        long activeCars   = carRepo.findAll().stream().filter(c -> c.getDeletedAt() == null && c.getStatus() == CarStatus.ACTIVE).count();
        long inactiveCars = carRepo.findAll().stream().filter(c -> c.getDeletedAt() == null && c.getStatus() != CarStatus.ACTIVE).count();
        long activeTours   = tourRepo.findAll().stream().filter(t -> t.getDeletedAt() == null && t.getStatus() == TourStatus.ACTIVE).count();
        long inactiveTours = tourRepo.findAll().stream().filter(t -> t.getDeletedAt() == null && t.getStatus() != TourStatus.ACTIVE).count();
        long activeDayTrips   = dayTripRepo.findAll().stream().filter(t -> t.getDeletedAt() == null && t.getStatus() == TourStatus.ACTIVE).count();
        long inactiveDayTrips = dayTripRepo.findAll().stream().filter(t -> t.getDeletedAt() == null && t.getStatus() != TourStatus.ACTIVE).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookings", Map.of(
                "total", totalBookings,
                "pending", pendingBookings,
                "confirmed", confirmedBookings,
                "cancelled", cancelledBookings));
        result.put("revenue", Map.of(
                "total", revenueTotal,
                "thisMonth", revenueThisMonth));
        result.put("agents", Map.of(
                "total", totalAgents,
                "pending", pendingAgents,
                "active", activeAgents));
        // Per-module booking counts (current + upcoming scheduled) — used by the new
        // Dashboard cards for Car Rental / Car Transfer / Activities (Tours) / Day Tour.
        result.put("modules", Map.of(
                "carRental",   moduleStats(BookingItemType.CAR_RENTAL,   currentByType, upcomingByType, revenueByType, revenueThisMonthByType),
                "carTransfer", moduleStats(BookingItemType.CAR_TRANSFER, currentByType, upcomingByType, revenueByType, revenueThisMonthByType),
                "activities",  moduleStats(BookingItemType.TOUR,         currentByType, upcomingByType, revenueByType, revenueThisMonthByType),
                "dayTour",     moduleStats(BookingItemType.DAY_TRIP,     currentByType, upcomingByType, revenueByType, revenueThisMonthByType)));
        result.put("assets", Map.of(
                "activeCars", activeCars,
                "inactiveCars", inactiveCars,
                "activeTours", activeTours,
                "inactiveTours", inactiveTours,
                "activeDayTrips", activeDayTrips,
                "inactiveDayTrips", inactiveDayTrips));
        return ApiResponse.ok(result);
    }

    private static Map<String, Long> moduleStats(BookingItemType type,
            Map<BookingItemType, Long> current,
            Map<BookingItemType, Long> upcoming,
            Map<BookingItemType, Long> revenue,
            Map<BookingItemType, Long> revenueThisMonth) {
        return Map.of(
                "currentBookings",  current.getOrDefault(type, 0L),
                "upcomingBookings", upcoming.getOrDefault(type, 0L),
                "revenueTotal",     revenue.getOrDefault(type, 0L),
                "revenueThisMonth", revenueThisMonth.getOrDefault(type, 0L));
    }
}
