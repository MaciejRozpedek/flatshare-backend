package com.flatshareteam.flatsharebackend.listings.dto;

import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;

import java.time.Instant;

public record CreateListingResponse(
        String listingId,
        ListingStatus status,
        Instant createdAt
) {
}
