package com.flatshareteam.flatsharebackend.listings.dto;

import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;

import java.util.UUID;

public record ListingStatusResponse(
        UUID listingId,
        ListingStatus status
) {
}
