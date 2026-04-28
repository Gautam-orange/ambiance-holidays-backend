package com.ambianceholidays.api.driver;

import com.ambianceholidays.api.driver.dto.*;
import com.ambianceholidays.common.dto.PageMeta;
import com.ambianceholidays.domain.car.Car;
import com.ambianceholidays.domain.car.CarRepository;
import com.ambianceholidays.domain.driver.*;
import com.ambianceholidays.domain.user.User;
import com.ambianceholidays.domain.user.UserRepository;
import com.ambianceholidays.exception.BusinessException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverAssignmentRepository assignmentRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        if (driverRepository.existsByLicenseNoAndDeletedAtIsNull(request.getLicenseNo())) {
            throw BusinessException.conflict("LICENSE_EXISTS", "License number already registered");
        }
        Driver driver = Driver.builder()
                .code(request.getCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .licenseNo(request.getLicenseNo())
                .licenseExpiry(request.getLicenseExpiry())
                .experienceYears((short) request.getExperienceYears())
                .photoUrl(request.getPhotoUrl())
                .build();
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse updateDriver(UUID id, DriverRequest request) {
        Driver driver = findOrThrow(id);
        driver.setCode(request.getCode());
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setPhone(request.getPhone());
        driver.setEmail(request.getEmail());
        driver.setAddress(request.getAddress());
        driver.setLicenseNo(request.getLicenseNo());
        driver.setLicenseExpiry(request.getLicenseExpiry());
        driver.setExperienceYears((short) request.getExperienceYears());
        driver.setPhotoUrl(request.getPhotoUrl());
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID id) {
        return DriverResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listDrivers(int page, int size, String search, DriverStatus status) {
        String q = blankToNull(search);
        Specification<Driver> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.isNull(root.get("deletedAt")));
            if (q != null) {
                String pattern = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern)
                ));
            }
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<Driver> pageResult = driverRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<DriverResponse> items = pageResult.getContent().stream().map(DriverResponse::from).toList();
        return Map.of("items", items, "meta", PageMeta.of(page, size, pageResult.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDriverWithAssignments(UUID id) {
        Driver driver = findOrThrow(id);
        List<AssignmentResponse> assignments = assignmentRepository
                .findByDriverIdOrderByStartAtDesc(id).stream()
                .map(AssignmentResponse::from).toList();
        return Map.of("driver", DriverResponse.from(driver), "assignments", assignments);
    }

    @Transactional
    public void deleteDriver(UUID id) {
        Driver driver = findOrThrow(id);
        driver.softDelete();
        driverRepository.save(driver);
    }

    @Transactional
    public DriverResponse updateStatus(UUID id, DriverStatus status) {
        Driver driver = findOrThrow(id);
        driver.setStatus(status);
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> listAvailableDrivers(Instant from, Instant to) {
        return driverRepository.findAllActive().stream()
                .filter(d -> d.getStatus() != DriverStatus.OFF_DUTY)
                .filter(d -> assignmentRepository.countOverlapping(d.getId(), from, to) == 0)
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional
    public AssignmentResponse assignDriver(UUID bookingItemId, AssignDriverRequest request, UUID assignedByUserId) {
        Driver driver = driverRepository.findByIdAndDeletedAtIsNull(request.getDriverId())
                .orElseThrow(() -> BusinessException.notFound("Driver not found"));

        if (assignmentRepository.countOverlapping(driver.getId(), request.getStartAt(), request.getEndAt()) > 0) {
            throw BusinessException.conflict("DRIVER_CONFLICT", "Driver already has an assignment in this time window");
        }

        Car car = carRepository.findByIdAndDeletedAtIsNull(request.getCarId())
                .orElseThrow(() -> BusinessException.notFound("Car not found"));

        User assignedBy = userRepository.findByIdAndDeletedAtIsNull(assignedByUserId).orElse(null);

        DriverAssignment assignment = DriverAssignment.builder()
                .driver(driver)
                .bookingItemId(bookingItemId)
                .car(car)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .notes(request.getNotes())
                .assignedBy(assignedBy)
                .build();

        assignment = assignmentRepository.save(assignment);

        // Update driver status
        long future = assignmentRepository.countOverlapping(driver.getId(), Instant.now(), Instant.now().plusSeconds(86400));
        driver.setStatus(future > 0 ? DriverStatus.BOOKED : DriverStatus.PARTIALLY_FREE);
        driverRepository.save(driver);

        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public void removeAssignment(UUID assignmentId) {
        DriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> BusinessException.notFound("Assignment not found"));
        assignmentRepository.delete(assignment);

        Driver driver = assignment.getDriver();
        long remaining = assignmentRepository.findByDriverIdOrderByStartAtDesc(driver.getId()).size();
        if (remaining == 0) driver.setStatus(DriverStatus.FREE);
        else driver.setStatus(DriverStatus.PARTIALLY_FREE);
        driverRepository.save(driver);
    }

    private Driver findOrThrow(UUID id) {
        return driverRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> BusinessException.notFound("Driver not found"));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
