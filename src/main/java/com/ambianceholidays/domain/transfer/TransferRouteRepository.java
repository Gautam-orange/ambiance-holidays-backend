package com.ambianceholidays.domain.transfer;

import com.ambianceholidays.domain.car.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRouteRepository extends JpaRepository<TransferRoute, UUID> {
    List<TransferRoute> findByActiveTrue();
    List<TransferRoute> findByFromLocationContainingIgnoreCaseAndToLocationContainingIgnoreCaseAndActiveTrue(
            String from, String to);
    List<TransferRoute> findByFromLocationIgnoreCaseAndToLocationIgnoreCaseAndTripTypeAndActiveTrue(
            String from, String to, TransferTripType tripType);
    List<TransferRoute> findByCarCategoryAndActiveTrue(CarCategory carCategory);
}
