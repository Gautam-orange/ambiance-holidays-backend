package com.ambianceholidays.domain.tour;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DayTripRepository extends JpaRepository<DayTrip, UUID>, JpaSpecificationExecutor<DayTrip> {
    Optional<DayTrip> findBySlug(String slug);
}
