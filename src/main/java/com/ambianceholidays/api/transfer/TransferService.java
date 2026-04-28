package com.ambianceholidays.api.transfer;

import com.ambianceholidays.api.transfer.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.transfer.*;
import com.ambianceholidays.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransferService {

    private final TransferRouteRepository repo;

    public TransferService(TransferRouteRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TransferRouteResponse>> listAll() {
        return ApiResponse.ok(repo.findAll().stream().map(TransferRouteResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TransferRouteResponse>> search(String from, String to) {
        List<TransferRoute> routes = (from != null && !from.isBlank() && to != null && !to.isBlank())
                ? repo.findByFromLocationContainingIgnoreCaseAndToLocationContainingIgnoreCaseAndActiveTrue(from, to)
                : repo.findByActiveTrue();
        return ApiResponse.ok(routes.stream().map(TransferRouteResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public ApiResponse<TransferRouteResponse> getById(UUID id) {
        TransferRoute r = repo.findById(id).orElseThrow(() -> BusinessException.notFound("TransferRoute"));
        return ApiResponse.ok(TransferRouteResponse.from(r));
    }

    public ApiResponse<TransferRouteResponse> create(TransferRouteRequest req) {
        TransferRoute r = new TransferRoute();
        apply(r, req);
        repo.save(r);
        return ApiResponse.ok(TransferRouteResponse.from(r));
    }

    public ApiResponse<TransferRouteResponse> update(UUID id, TransferRouteRequest req) {
        TransferRoute r = repo.findById(id).orElseThrow(() -> BusinessException.notFound("TransferRoute"));
        apply(r, req);
        repo.save(r);
        return ApiResponse.ok(TransferRouteResponse.from(r));
    }

    public ApiResponse<Void> delete(UUID id) {
        TransferRoute r = repo.findById(id).orElseThrow(() -> BusinessException.notFound("TransferRoute"));
        repo.delete(r);
        return ApiResponse.ok(null);
    }

    private void apply(TransferRoute r, TransferRouteRequest req) {
        r.setFromLocation(req.fromLocation());
        r.setToLocation(req.toLocation());
        r.setTripType(req.tripType());
        r.setCarCategory(req.carCategory());
        r.setBasePriceCents(req.basePriceCents());
        r.setEstDurationMins(req.estDurationMins());
        r.setEstKm(req.estKm());
        r.setActive(req.active());
    }
}
