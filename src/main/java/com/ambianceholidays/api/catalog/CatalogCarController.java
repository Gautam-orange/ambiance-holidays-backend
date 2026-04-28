package com.ambianceholidays.api.catalog;

import com.ambianceholidays.api.car.CarService;
import com.ambianceholidays.api.car.dto.CarResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.car.CarCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/catalog/cars")
@RequiredArgsConstructor
public class CatalogCarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) CarCategory category,
            @RequestParam(required = false) Integer minPax) {
        return ResponseEntity.ok(ApiResponse.ok(carService.listCatalogCars(page, size, category, minPax)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarResponse>> getCar(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(carService.getCar(id)));
    }
}
