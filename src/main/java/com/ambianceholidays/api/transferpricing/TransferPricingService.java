package com.ambianceholidays.api.transferpricing;

import com.ambianceholidays.api.transferpricing.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.transferpricing.TransferPricingTier;
import com.ambianceholidays.domain.transferpricing.TransferPricingTierRepository;
import com.ambianceholidays.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransferPricingService {

    private final TransferPricingTierRepository repo;

    public TransferPricingService(TransferPricingTierRepository repo) {
        this.repo = repo;
    }

    // ── Admin: CRUD ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<TransferPricingTierResponse>> listAll() {
        return ApiResponse.ok(repo.findAll().stream()
                .sorted((a, b) -> a.getMinKm() - b.getMinKm())
                .map(TransferPricingTierResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public ApiResponse<TransferPricingTierResponse> getById(UUID id) {
        return ApiResponse.ok(TransferPricingTierResponse.from(
                repo.findById(id).orElseThrow(() -> BusinessException.notFound("TransferPricingTier"))));
    }

    public ApiResponse<TransferPricingTierResponse> create(TransferPricingTierRequest req) {
        TransferPricingTier t = new TransferPricingTier();
        apply(t, req);
        repo.save(t);
        return ApiResponse.ok(TransferPricingTierResponse.from(t));
    }

    public ApiResponse<TransferPricingTierResponse> update(UUID id, TransferPricingTierRequest req) {
        TransferPricingTier t = repo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("TransferPricingTier"));
        apply(t, req);
        repo.save(t);
        return ApiResponse.ok(TransferPricingTierResponse.from(t));
    }

    public ApiResponse<Void> delete(UUID id) {
        repo.findById(id).orElseThrow(() -> BusinessException.notFound("TransferPricingTier"));
        repo.deleteById(id);
        return ApiResponse.ok(null);
    }

    // ── Public: price quote by distance ───────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<TransferPricingTierResponse>> listActive() {
        return ApiResponse.ok(
                repo.findAllByActiveTrueOrderBySortOrderAscMinKmAsc()
                        .stream().map(TransferPricingTierResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public ApiResponse<TransferPriceQuoteResponse> quoteByDistance(int distanceKm) {
        Optional<TransferPricingTier> match = repo.findForDistance(distanceKm);
        if (match.isEmpty()) {
            return ApiResponse.ok(new TransferPriceQuoteResponse(null, null, distanceKm, 0, false));
        }
        TransferPricingTier t = match.get();
        return ApiResponse.ok(new TransferPriceQuoteResponse(
                t.getId(), t.getLabel(), distanceKm, t.getPriceCents(), true));
    }

    // ── Helpers ───────────────────────────────────────────────

    private void apply(TransferPricingTier t, TransferPricingTierRequest req) {
        t.setLabel(req.label());
        t.setMinKm(req.minKm());
        t.setMaxKm(req.maxKm());
        t.setPriceCents(req.priceCents());
        t.setActive(req.active());
        t.setSortOrder(req.sortOrder());
        t.setIncludes(req.includes() == null ? new String[0] : req.includes().toArray(new String[0]));
        t.setExcludes(req.excludes() == null ? new String[0] : req.excludes().toArray(new String[0]));
    }
}
