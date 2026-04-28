package com.ambianceholidays.api.driver;

import com.ambianceholidays.api.driver.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.driver.DriverStatus;
import com.ambianceholidays.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
public class AdminDriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DriverStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.listDrivers(page, size, search, status)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(driverService.createDriver(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDriver(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getDriverWithAssignments(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable UUID id, @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateDriver(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DriverResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam DriverStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateStatus(id, status)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> listAvailable(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.listAvailableDrivers(from, to)));
    }

    @PostMapping("/assignments/{bookingItemId}/assign")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignDriver(
            @PathVariable UUID bookingItemId,
            @Valid @RequestBody AssignDriverRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(driverService.assignDriver(bookingItemId, request, principal.getUserId())));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable UUID assignmentId) {
        driverService.removeAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
