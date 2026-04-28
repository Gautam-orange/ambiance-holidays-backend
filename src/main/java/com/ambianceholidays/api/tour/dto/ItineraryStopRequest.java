package com.ambianceholidays.api.tour.dto;

public record ItineraryStopRequest(String stopTime, String title, String description, short sortOrder) {}
