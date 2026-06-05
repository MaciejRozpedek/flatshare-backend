package com.flatshareteam.flatsharebackend.listings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UnavailabilityRequest(
        @NotNull(message = "Since date is mandatory")
        @JsonProperty("Since")
        LocalDate since,

        @NotNull(message = "Until date is mandatory")
        @JsonProperty("Until")
        LocalDate until,

        @JsonProperty("Message")
        String message
) {
}
