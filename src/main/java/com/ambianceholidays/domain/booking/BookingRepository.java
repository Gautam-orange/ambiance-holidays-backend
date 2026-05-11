package com.ambianceholidays.domain.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    Optional<Booking> findByReference(String reference);
    boolean existsByReference(String reference);

    /** Pending / confirmed bookings whose service date is today and which
     *  haven't received the morning reminder yet. Native because the postgres
     *  booking_status enum needs an explicit text cast. */
    @Query(value = "SELECT * FROM bookings WHERE deleted_at IS NULL "
            + "AND status::text IN ('PENDING','CONFIRMED') "
            + "AND service_date = :today "
            + "AND reminder_day_sent_at IS NULL", nativeQuery = true)
    List<Booking> findDueForDayReminder(@Param("today") LocalDate today);

    /** Confirmed bookings whose earliest item startAt is between now and the
     *  given upper bound and which haven't been nudged yet. */
    @Query(value = "SELECT DISTINCT b.* FROM bookings b "
            + "JOIN booking_items i ON i.booking_id = b.id "
            + "WHERE b.deleted_at IS NULL "
            + "AND b.status::text = 'CONFIRMED' "
            + "AND i.start_at IS NOT NULL "
            + "AND i.start_at BETWEEN :from AND :to "
            + "AND b.reminder_imminent_sent_at IS NULL", nativeQuery = true)
    List<Booking> findDueForImminentReminder(@Param("from") Instant from, @Param("to") Instant to);
}
