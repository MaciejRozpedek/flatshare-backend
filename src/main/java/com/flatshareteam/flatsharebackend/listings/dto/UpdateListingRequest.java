package com.flatshareteam.flatsharebackend.listings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateListingRequest(
        String title,

        String description,

        @Positive(message = "Price must be strictly greater than 0")
        BigDecimal price,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        LocalDate availableSince,

        LocalDate availableUntil,

        String ownerContact,

        @Positive(message = "Area must be positive")
        Double area,

        @Valid
        Location location,

        @Valid
        Attributes attributes
) {
    public record Location(
            String city,
            String district,
            String street,
            String aptNumber
    ) {}

    public record Attributes(
            Boolean petsAllowed,
            Boolean nonSmokingOnly,
            Boolean closeToShops,
            String profile
    ) {}
}