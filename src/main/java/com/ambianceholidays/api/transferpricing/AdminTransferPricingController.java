package com.ambianceholidays.api.transferpricing;

import com.ambianceholidays.api.transferpricing.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/transfer-pricing")
public class AdminTransferPricingController {

    private final TransferPricingService service;

    public AdminTransferPricingController(TransferPricingService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<TransferPricingTierResponse>> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<TransferPricingTierResponse> get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TransferPricingTierResponse> create(@Valid @RequestBody TransferPricingTierRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TransferPricingTierResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody TransferPricingTierRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
