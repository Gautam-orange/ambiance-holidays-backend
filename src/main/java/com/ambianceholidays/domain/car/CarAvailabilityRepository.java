package com.ambianceholidays.domain.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CarAvailabilityRepository extends JpaRepository<CarAvailability, UUID> {

    @Query("""
        SELECT a FROM CarAvailability a
        WHERE a.car.id = :carId
          AND a.dateTo >= :monthStart
          AND a.dateFrom <= :monthEnd
        """)
    List<CarAvailability> findByCarIdInMonth(
            @Param("carId") UUID carId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);

    @Query("""
        SELECT a FROM CarAvailability a
        WHERE a.car.id IN :carIds
          AND a.dateTo >= :monthStart
          AND a.dateFrom <= :monthEnd
        """)
    List<CarAvailability> findByCarsInMonth(
            @Param("carIds") List<UUID> carIds,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);

    @Query("""
        SELECT COUNT(a) FROM CarAvailability a
        WHERE a.car.id = :carId
          AND a.dateTo >= :from
          AND a.dateFrom <= :to
        """)
    long countOverlapping(
            @Param("carId") UUID carId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
