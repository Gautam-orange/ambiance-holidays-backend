package com.ambianceholidays.domain.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByDeletedAtIsNullAndActiveTrue();
    Optional<Supplier> findByIdAndDeletedAtIsNull(UUID id);
}
