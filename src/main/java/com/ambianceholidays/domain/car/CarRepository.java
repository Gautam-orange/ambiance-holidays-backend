package com.ambianceholidays.domain.car;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

    Optional<Car> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByRegistrationNoAndDeletedAtIsNull(String registrationNo);

    @Query("SELECT c FROM Car c WHERE c.deletedAt IS NULL AND c.status = 'ACTIVE' ORDER BY c.name")
    List<Car> findAllActive();
}
