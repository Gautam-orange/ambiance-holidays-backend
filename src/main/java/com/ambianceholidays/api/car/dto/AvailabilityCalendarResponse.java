package com.ambianceholidays.api.car.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AvailabilityCalendarResponse {
    private int year;
    private int month;
    private int daysInMonth;
    private List<CarCalendarRow> cars;

    @Data
    public static class CarCalendarRow {
        private UUID carId;
        private String registrationNo;
        private String name;
        private String coverImageUrl;
        private String category;
        private List<BlockedRange> blockedRanges;

        @Data
        public static class BlockedRange {
            private UUID availabilityId;
            private LocalDate dateFrom;
            private LocalDate dateTo;
            private String reason;
        }
    }
}
