package com.ambianceholidays.api.tour.dto;

import java.time.LocalTime;

public record PickupZoneRequest(String zoneName, int extraCents, LocalTime pickupTime) {}
