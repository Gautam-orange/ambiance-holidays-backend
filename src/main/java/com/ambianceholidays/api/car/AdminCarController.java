package com.ambianceholidays.api.car;

import com.ambianceholidays.api.car.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.car.CarCategory;
import com.ambianceholidays.domain.car.CarUsageType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/cars")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
public class AdminCarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CarCategory category,
            @RequestParam(required = false) CarUsageType usageType) {
        Map<String, Object> result = carService.listCars(page, size, search, category, usageType);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<CarResponse>> createCar(@Valid @RequestBody CarRequest request) {
        CarResponse car = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(car));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarResponse>> getCar(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(carService.getCar(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(
            @PathVariable UUID id,
            @Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(carService.updateCar(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ResponseEntity<Void> deleteCar(@PathVariable UUID id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<CarResponse>> toggleStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(carService.toggleStatus(id)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityCalendarResponse>> getAvailability(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(carService.getAvailabilityCalendar(year, month)));
    }

    @PostMapping("/{id}/availability/block")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<AvailabilityCalendarResponse.CarCalendarRow.BlockedRange>> blockDates(
            @PathVariable UUID id,
            @Valid @RequestBody BlockDatesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(carService.blockDates(id, request)));
    }

    @DeleteMapping("/availability/{availabilityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<Void> unblockDates(@PathVariable UUID availabilityId) {
        carService.unblockDates(availabilityId);
        return ResponseEntity.noContent().build();
    }
}
