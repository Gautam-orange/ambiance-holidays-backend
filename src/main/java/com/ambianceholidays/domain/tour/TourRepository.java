package com.ambianceholidays.domain.tour;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TourRepository extends JpaRepository<Tour, UUID>, JpaSpecificationExecutor<Tour> {
    Optional<Tour> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
