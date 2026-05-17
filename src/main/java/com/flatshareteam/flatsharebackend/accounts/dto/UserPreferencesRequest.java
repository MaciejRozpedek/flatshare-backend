package com.flatshareteam.flatsharebackend.accounts.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UserPreferencesRequest(
    BigDecimal maxPrice,

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency,

    Boolean smokingAllowed,

        Boolean petsAllowed,

    List<String> preferredDistricts
) {
}