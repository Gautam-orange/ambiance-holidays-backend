package com.ambianceholidays.domain.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, UUID> {

    List<DriverAssignment> findByDriverIdOrderByStartAtDesc(UUID driverId);

    Optional<DriverAssignment> findByBookingItemId(UUID bookingItemId);

    @Query("""
        SELECT COUNT(a) FROM DriverAssignment a
        WHERE a.driver.id = :driverId
          AND a.endAt > :from
          AND a.startAt < :to
        """)
    long countOverlapping(
            @Param("driverId") UUID driverId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
        SELECT a FROM DriverAssignment a
        WHERE a.driver.id = :driverId
          AND a.endAt >= :from
          AND a.startAt <= :to
        ORDER BY a.startAt
        """)
    List<DriverAssignment> findByDriverInRange(
            @Param("driverId") UUID driverId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
