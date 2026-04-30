package com.ambianceholidays.api.car;

import com.ambianceholidays.api.car.dto.*;
import com.ambianceholidays.common.dto.PageMeta;
import com.ambianceholidays.domain.car.*;
import com.ambianceholidays.domain.supplier.Supplier;
import com.ambianceholidays.domain.supplier.SupplierRepository;
import com.ambianceholidays.exception.BusinessException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CarRateRepository carRateRepository;
    private final CarAvailabilityRepository availabilityRepository;
    private final SupplierRepository supplierRepository;

    @Transactional
    public CarResponse createCar(CarRequest request) {
        if (carRepository.existsByRegistrationNoAndDeletedAtIsNull(request.getRegistrationNo())) {
            throw BusinessException.conflict("REGISTRATION_EXISTS", "Registration number already exists");
        }

        validateRates(request);

        Supplier supplier = resolveSupplier(request.getSupplierId());

        Car car = Car.builder()
                .registrationNo(request.getRegistrationNo().trim().toUpperCase())
                .name(request.getName())
                .category(request.getCategory())
                .usageType(request.getUsageType())
                .year((short) request.getYear())
                .passengerCapacity((short) request.getPassengerCapacity())
                .luggageCapacity(request.getLuggageCapacity() != null ? request.getLuggageCapacity().shortValue() : null)
                .hasAc(request.isHasAc())
                .automatic(request.isAutomatic())
                .fuelType(request.getFuelType() != null ? request.getFuelType() : "Petrol")
                .color(request.getColor())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .galleryUrls(toArray(request.getGalleryUrls()))
                .includes(toArray(request.getIncludes()))
                .excludes(toArray(request.getExcludes()))
                .supplier(supplier)
                .build();

        car = carRepository.save(car);

        List<CarRate> rates = saveRates(car, request.getRates());
        return CarResponse.from(car, rates);
    }

    @Transactional
    public CarResponse updateCar(UUID id, CarRequest request) {
        Car car = findCarOrThrow(id);

        if (!car.getRegistrationNo().equals(request.getRegistrationNo().trim().toUpperCase())) {
            if (carRepository.existsByRegistrationNoAndDeletedAtIsNull(request.getRegistrationNo())) {
                throw BusinessException.conflict("REGISTRATION_EXISTS", "Registration number already in use");
            }
        }

        validateRates(request);

        Supplier supplier = resolveSupplier(request.getSupplierId());

        car.setRegistrationNo(request.getRegistrationNo().trim().toUpperCase());
        car.setName(request.getName());
        car.setCategory(request.getCategory());
        car.setUsageType(request.getUsageType());
        car.setYear((short) request.getYear());
        car.setPassengerCapacity((short) request.getPassengerCapacity());
        car.setLuggageCapacity(request.getLuggageCapacity() != null ? request.getLuggageCapacity().shortValue() : null);
        car.setHasAc(request.isHasAc());
        car.setAutomatic(request.isAutomatic());
        car.setFuelType(request.getFuelType() != null ? request.getFuelType() : "Petrol");
        car.setColor(request.getColor());
        car.setDescription(request.getDescription());
        car.setCoverImageUrl(request.getCoverImageUrl());
        car.setGalleryUrls(toArray(request.getGalleryUrls()));
        car.setIncludes(toArray(request.getIncludes()));
        car.setExcludes(toArray(request.getExcludes()));
        car.setSupplier(supplier);

        car = carRepository.save(car);

        carRateRepository.deleteByCarId(car.getId());
        List<CarRate> rates = saveRates(car, request.getRates());
        return CarResponse.from(car, rates);
    }

    @Transactional(readOnly = true)
    public CarResponse getCar(UUID id) {
        Car car = findCarOrThrow(id);
        List<CarRate> rates = carRateRepository.findByCarId(id);
        return CarResponse.from(car, rates);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listCars(int page, int size, String search, CarCategory category, CarUsageType usageType) {
        String q = blankToNull(search);
        Specification<Car> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (q != null) {
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("registrationNo")), pattern)
                ));
            }
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (usageType != null) predicates.add(cb.or(
                    cb.equal(root.get("usageType"), usageType),
                    cb.equal(root.get("usageType"), CarUsageType.BOTH)
            ));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Car> pageResult = carRepository.findAll(spec, pageable);
        return buildPageResponse(page, size, pageResult);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listCatalogCars(int page, int size, CarCategory category, Integer minPax) {
        Specification<Car> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), CarStatus.ACTIVE));
            predicates.add(cb.or(
                    cb.equal(root.get("usageType"), CarUsageType.RENTAL),
                    cb.equal(root.get("usageType"), CarUsageType.BOTH)
            ));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (minPax != null) predicates.add(cb.greaterThanOrEqualTo(root.get("passengerCapacity"), (short) minPax.intValue()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(page, size, Sort.by("category").ascending().and(Sort.by("name").ascending()));
        Page<Car> pageResult = carRepository.findAll(spec, pageable);
        return buildPageResponse(page, size, pageResult);
    }

    private Map<String, Object> buildPageResponse(int page, int size, Page<Car> pageResult) {
        List<UUID> ids = pageResult.getContent().stream().map(Car::getId).toList();
        Map<UUID, List<CarRate>> ratesByCarId = ids.isEmpty() ? Map.of() :
                carRateRepository.findByCarIdIn(ids).stream()
                        .collect(Collectors.groupingBy(r -> r.getCar().getId()));
        List<CarResponse> items = pageResult.getContent().stream()
                .map(car -> CarResponse.from(car, ratesByCarId.getOrDefault(car.getId(), List.of())))
                .toList();
        return Map.of("items", items, "meta", PageMeta.of(page, size, pageResult.getTotalElements()));
    }

    @Transactional
    public void deleteCar(UUID id) {
        Car car = findCarOrThrow(id);
        car.softDelete();
        carRepository.save(car);
    }

    @Transactional
    public CarResponse toggleStatus(UUID id) {
        Car car = findCarOrThrow(id);
        car.setStatus(car.getStatus() == CarStatus.ACTIVE ? CarStatus.INACTIVE : CarStatus.ACTIVE);
        car = carRepository.save(car);
        List<CarRate> rates = carRateRepository.findByCarId(id);
        return CarResponse.from(car, rates);
    }

    @Transactional(readOnly = true)
    public AvailabilityCalendarResponse getAvailabilityCalendar(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<Car> cars = carRepository.findAllActive();
        List<UUID> carIds = cars.stream().map(Car::getId).toList();

        List<CarAvailability> blocks = carIds.isEmpty() ? List.of() :
                availabilityRepository.findByCarsInMonth(carIds, monthStart, monthEnd);

        Map<UUID, List<CarAvailability>> blocksByCar = blocks.stream()
                .collect(Collectors.groupingBy(a -> a.getCar().getId()));

        List<AvailabilityCalendarResponse.CarCalendarRow> rows = cars.stream().map(car -> {
            AvailabilityCalendarResponse.CarCalendarRow row = new AvailabilityCalendarResponse.CarCalendarRow();
            row.setCarId(car.getId());
            row.setRegistrationNo(car.getRegistrationNo());
            row.setName(car.getName());
            row.setCoverImageUrl(car.getCoverImageUrl());
            row.setCategory(car.getCategory().name());

            List<AvailabilityCalendarResponse.CarCalendarRow.BlockedRange> ranges =
                    blocksByCar.getOrDefault(car.getId(), List.of()).stream().map(a -> {
                        AvailabilityCalendarResponse.CarCalendarRow.BlockedRange br =
                                new AvailabilityCalendarResponse.CarCalendarRow.BlockedRange();
                        br.setAvailabilityId(a.getId());
                        br.setDateFrom(a.getDateFrom());
                        br.setDateTo(a.getDateTo());
                        br.setReason(a.getReason());
                        return br;
                    }).toList();
            row.setBlockedRanges(ranges);
            return row;
        }).toList();

        AvailabilityCalendarResponse response = new AvailabilityCalendarResponse();
        response.setYear(year);
        response.setMonth(month);
        response.setDaysInMonth(ym.lengthOfMonth());
        response.setCars(rows);
        return response;
    }

    @Transactional
    public AvailabilityCalendarResponse.CarCalendarRow.BlockedRange blockDates(UUID carId, BlockDatesRequest request) {
        Car car = findCarOrThrow(carId);

        if (request.getDateTo().isBefore(request.getDateFrom())) {
            throw BusinessException.badRequest("INVALID_DATES", "dateTo must be on or after dateFrom");
        }

        CarAvailability block = CarAvailability.builder()
                .car(car)
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .reason(request.getReason() != null ? request.getReason() : "BLOCKED")
                .build();
        block = availabilityRepository.save(block);

        AvailabilityCalendarResponse.CarCalendarRow.BlockedRange br =
                new AvailabilityCalendarResponse.CarCalendarRow.BlockedRange();
        br.setAvailabilityId(block.getId());
        br.setDateFrom(block.getDateFrom());
        br.setDateTo(block.getDateTo());
        br.setReason(block.getReason());
        return br;
    }

    @Transactional
    public void unblockDates(UUID availabilityId) {
        CarAvailability block = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> BusinessException.notFound("Availability block not found"));
        availabilityRepository.delete(block);
    }

    // --- helpers ---

    private Car findCarOrThrow(UUID id) {
        return carRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> BusinessException.notFound("Car not found"));
    }

    private Supplier resolveSupplier(UUID supplierId) {
        if (supplierId == null) return null;
        return supplierRepository.findByIdAndDeletedAtIsNull(supplierId)
                .orElseThrow(() -> BusinessException.notFound("Supplier not found"));
    }

    /**
     * Validate the rates submitted with a CarRequest. Rules:
     *  - Transfer-eligible cars (TRANSFER, BOTH) must have at least one PER_KM band.
     *  - Each band's amountCents must be ≥ 0.
     *  - kmFrom must be ≥ 0; if kmTo is set it must be > kmFrom.
     *  - PER_KM bands on the same car must not overlap.
     *  Rental periods (DAILY/WEEKLY/MONTHLY) only require amountCents ≥ 0.
     */
    private void validateRates(CarRequest request) {
        List<CarRateRequest> rates = request.getRates();
        boolean isTransferEligible = request.getUsageType() == CarUsageType.TRANSFER
                || request.getUsageType() == CarUsageType.BOTH;

        // Per-row checks
        if (rates != null) {
            for (int i = 0; i < rates.size(); i++) {
                CarRateRequest r = rates.get(i);
                if (r.getAmountCents() == null || r.getAmountCents() < 0) {
                    throw BusinessException.badRequest("INVALID_RATE",
                            "Rate row " + (i + 1) + ": price must be a non-negative number.");
                }
                if (r.getPeriod() == RatePeriod.PER_KM) {
                    int from = r.getKmFrom() != null ? r.getKmFrom() : 0;
                    if (from < 0) {
                        throw BusinessException.badRequest("INVALID_RATE",
                                "Transfer band " + (i + 1) + ": From-km cannot be negative.");
                    }
                    if (r.getKmTo() != null && r.getKmTo() <= from) {
                        throw BusinessException.badRequest("INVALID_RATE",
                                "Transfer band " + (i + 1) + ": To-km must be greater than From-km.");
                    }
                }
            }
        }

        // Transfer-specific overlap + presence rules
        if (isTransferEligible) {
            List<CarRateRequest> perKm = rates == null ? List.of() :
                    rates.stream().filter(r -> r.getPeriod() == RatePeriod.PER_KM).toList();
            if (perKm.isEmpty()) {
                throw BusinessException.badRequest("MISSING_TRANSFER_RATES",
                        "Transfer-eligible cars need at least one PER_KM rate band.");
            }
            // Sort by kmFrom and check for overlaps
            var sorted = new ArrayList<>(perKm);
            sorted.sort((a, b) -> {
                int af = a.getKmFrom() != null ? a.getKmFrom() : 0;
                int bf = b.getKmFrom() != null ? b.getKmFrom() : 0;
                return Integer.compare(af, bf);
            });
            for (int i = 1; i < sorted.size(); i++) {
                CarRateRequest prev = sorted.get(i - 1);
                CarRateRequest curr = sorted.get(i);
                int prevTo = prev.getKmTo() != null ? prev.getKmTo() : Integer.MAX_VALUE;
                int currFrom = curr.getKmFrom() != null ? curr.getKmFrom() : 0;
                if (currFrom <= prevTo) {
                    throw BusinessException.badRequest("OVERLAPPING_BANDS",
                            "Transfer bands overlap: band ending at " + prevTo +
                            " km conflicts with band starting at " + currFrom + " km.");
                }
            }
        }
    }

    private List<CarRate> saveRates(Car car, List<CarRateRequest> rateRequests) {
        if (rateRequests == null || rateRequests.isEmpty()) return List.of();
        List<CarRate> rates = rateRequests.stream().map(req -> CarRate.builder()
                .car(car)
                .period(req.getPeriod())
                .amountCents(req.getAmountCents())
                .kmFrom(req.getKmFrom())
                .kmTo(req.getKmTo())
                .build()).toList();
        return carRateRepository.saveAll(rates);
    }

    private String[] toArray(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().filter(s -> s != null && !s.isBlank()).toArray(String[]::new);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
