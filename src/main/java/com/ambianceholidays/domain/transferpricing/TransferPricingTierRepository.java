package com.ambianceholidays.domain.transferpricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferPricingTierRepository extends JpaRepository<TransferPricingTier, UUID> {

    List<TransferPricingTier> findAllByActiveTrueOrderBySortOrderAscMinKmAsc();

    /** Find the tier whose range covers the given distance (active tiers only). */
    default Optional<TransferPricingTier> findForDistance(int distanceKm) {
        return findAllByActiveTrueOrderBySortOrderAscMinKmAsc().stream()
                .filter(t -> distanceKm >= t.getMinKm()
                        && (t.getMaxKm() == null || distanceKm <= t.getMaxKm()))
                .findFirst();
    }
}
