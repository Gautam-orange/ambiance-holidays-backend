package com.ambianceholidays.domain.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID>, JpaSpecificationExecutor<Driver> {
    Optional<Driver> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByLicenseNoAndDeletedAtIsNull(String licenseNo);

    @Query("SELECT d FROM Driver d WHERE d.deletedAt IS NULL AND d.active = true ORDER BY d.firstName, d.lastName")
    List<Driver> findAllActive();

    @Query("SELECT d FROM Driver d WHERE d.deletedAt IS NULL AND d.active = true AND d.status = 'FREE' ORDER BY d.experienceYears DESC")
    List<Driver> findAllFree();
}
