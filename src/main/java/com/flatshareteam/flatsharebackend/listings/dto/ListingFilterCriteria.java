package com.flatshareteam.flatsharebackend.listings.dto;

import java.util.UUID;

public record ListingFilterCriteria(
        String city,
        String district,
        String street,
        String aptNumber,
        UUID ownerID
) {
}
