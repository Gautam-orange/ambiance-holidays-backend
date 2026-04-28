package com.ambianceholidays.api.driver.dto;

import com.ambianceholidays.domain.driver.DriverAssignment;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AssignmentResponse {
    private UUID id;
    private UUID driverId;
    private String driverName;
    private UUID bookingItemId;
    private UUID carId;
    private String carName;
    private String carRegistrationNo;
    private Instant startAt;
    private Instant endAt;
    private String pickupAddress;
    private String dropoffAddress;
    private String notes;
    private Instant assignedAt;

    public static AssignmentResponse from(DriverAssignment a) {
        AssignmentResponse r = new AssignmentResponse();
        r.id = a.getId();
        r.driverId = a.getDriver().getId();
        r.driverName = a.getDriver().getFullName();
        r.bookingItemId = a.getBookingItemId();
        r.carId = a.getCar() != null ? a.getCar().getId() : null;
        r.carName = a.getCar() != null ? a.getCar().getName() : null;
        r.carRegistrationNo = a.getCar() != null ? a.getCar().getRegistrationNo() : null;
        r.startAt = a.getStartAt();
        r.endAt = a.getEndAt();
        r.pickupAddress = a.getPickupAddress();
        r.dropoffAddress = a.getDropoffAddress();
        r.notes = a.getNotes();
        r.assignedAt = a.getAssignedAt();
        return r;
    }
}
