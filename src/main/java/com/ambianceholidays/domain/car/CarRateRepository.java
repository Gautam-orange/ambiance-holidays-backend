package com.ambianceholidays.domain.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CarRateRepository extends JpaRepository<CarRate, UUID> {
    List<CarRate> findByCarId(UUID carId);
    void deleteByCarId(UUID carId);

    @Query("SELECT r FROM CarRate r WHERE r.car.id IN :carIds")
    List<CarRate> findByCarIdIn(@Param("carIds") List<UUID> carIds);
}
