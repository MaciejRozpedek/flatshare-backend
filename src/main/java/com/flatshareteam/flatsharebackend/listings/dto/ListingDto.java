package com.flatshareteam.flatsharebackend.listings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        Attributes attributes
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
}