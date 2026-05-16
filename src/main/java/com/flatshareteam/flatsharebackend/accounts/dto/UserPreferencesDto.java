package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;

import java.math.BigDecimal;
import java.util.List;

public record UserPreferencesDto(
    BigDecimal maxPrice,
    String currency,
    Boolean smokingAllowed,
    Boolean petsAllowed,
    List<String> preferredDistricts
) {
    public static UserPreferencesDto from(TenantRole tenantRole) {
        return new UserPreferencesDto(
        tenantRole.getMaxPrice(),
        tenantRole.getCurrency(),
        tenantRole.getSmokingAllowed(),
        tenantRole.getPetsAllowed(),
        List.copyOf(tenantRole.getPreferredDistricts())
        );
    }
}