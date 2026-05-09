package com.ambianceholidays.domain.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CarExtraServiceRepository extends JpaRepository<CarExtraService, UUID> {

    List<CarExtraService> findByCarIdOrderByDisplayOrderAsc(UUID carId);

    @Modifying
    @Query("DELETE FROM CarExtraService e WHERE e.car.id = :carId")
    void deleteByCarId(UUID carId);
}
