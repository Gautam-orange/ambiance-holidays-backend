package com.ambianceholidays.api.supplier;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.supplier.Supplier;
import com.ambianceholidays.domain.supplier.SupplierRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Lightweight read-only listing for the Add/Edit Car form's supplier picker.
 * Returns active, non-deleted suppliers with just id+name. Full supplier CRUD
 * is not yet exposed (no admin UI).
 */
@RestController
@RequestMapping("/admin/suppliers")
public class AdminSupplierController {

    public record SupplierOption(UUID id, String name) {
        static SupplierOption from(Supplier s) {
            return new SupplierOption(s.getId(), s.getName());
        }
    }

    private final SupplierRepository supplierRepo;

    public AdminSupplierController(SupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<SupplierOption>> list() {
        return ApiResponse.ok(
                supplierRepo.findByDeletedAtIsNullAndActiveTrue()
                        .stream()
                        .map(SupplierOption::from)
                        .toList());
    }
}
