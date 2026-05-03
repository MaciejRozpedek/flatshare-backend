package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.model.UserPreferences;

import java.math.BigDecimal;
import java.util.List;

public record UserPreferencesDto(
    BigDecimal maxPrice,
    String currency,
    Boolean smokingAllowed,
    Boolean petsAllowed,
    List<String> preferredDistricts
) {
    public static UserPreferencesDto from(UserPreferences preferences) {
        return new UserPreferencesDto(
        preferences.getMaxPrice(),
        preferences.getCurrency(),
        preferences.getSmokingAllowed(),
        preferences.getPetsAllowed(),
        List.copyOf(preferences.getPreferredDistricts())
        );
    }
}