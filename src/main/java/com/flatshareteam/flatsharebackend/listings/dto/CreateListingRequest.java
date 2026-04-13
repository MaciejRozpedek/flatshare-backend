package com.flatshareteam.flatsharebackend.listings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateListingRequest(
        @NotBlank(message = "Title is mandatory")
        String title,

        @NotBlank(message = "Description is mandatory")
        String description,

        @NotNull(message = "Price is mandatory")
        @Positive(message = "Price must be strictly greater than 0")
        BigDecimal price,

        @NotBlank(message = "Currency is mandatory")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        @NotNull(message = "Available since date is mandatory")
        LocalDate availableSince,

        @NotNull(message = "Available until date is mandatory")
        LocalDate availableUntil,

        @NotBlank(message = "Owner contact is mandatory")
        String ownerContact,

        @NotNull(message = "Area is mandatory")
        @Positive(message = "Area must be positive")
        Double area,

        @NotNull(message = "Location is mandatory")
        @Valid
        Location location,

        @NotNull(message = "Attributes are mandatory")
        @Valid
        Attributes attributes
) {
    public record Location(
            @NotBlank(message = "City is mandatory")
            String city,

            @NotBlank(message = "District is mandatory")
            String district,

            @NotBlank(message = "Street is mandatory")
            String street,

            @NotBlank(message = "Apartment number is mandatory")
            String aptNumber
    ) {}

    public record Attributes(
            @NotNull(message = "Pets allowed flag is mandatory")
            Boolean petsAllowed,

            @NotNull(message = "Non smoking only flag is mandatory")
            Boolean nonSmokingOnly,

            @NotNull(message = "Close to shops flag is mandatory")
            Boolean closeToShops,

            @NotBlank(message = "Profile is mandatory")
            String profile
    ) {}
}