package com.flatshareteam.flatsharebackend.listings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ListingDto(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        String currency,
        LocalDate availableSince,
        LocalDate availableUntil,
        String ownerContact,
        Double area,
        Location location,
        Attributes attributes,
        ListingStatus status,
        List<UnavailabilityDto> unavailability
) {
    public record Location(
            String city,
            String district,
            String street,
            String aptNumber
    ) {}

    public record Attributes(
            boolean petsAllowed,
            boolean nonSmokingOnly,
            boolean closeToShops,
            String profile
    ) {}

    public record UnavailabilityDto(
            @JsonProperty("Since") LocalDate since,
            @JsonProperty("Until") LocalDate until,
            @JsonProperty("Message") String message
    ) {}
}
