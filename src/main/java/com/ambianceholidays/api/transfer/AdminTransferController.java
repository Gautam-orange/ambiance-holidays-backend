package com.ambianceholidays.api.transfer;

import com.ambianceholidays.api.transfer.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/transfers")
public class AdminTransferController {

    private final TransferService service;

    public AdminTransferController(TransferService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<TransferRouteResponse>> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<TransferRouteResponse> get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TransferRouteResponse> create(@Valid @RequestBody TransferRouteRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<TransferRouteResponse> update(@PathVariable UUID id, @Valid @RequestBody TransferRouteRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
