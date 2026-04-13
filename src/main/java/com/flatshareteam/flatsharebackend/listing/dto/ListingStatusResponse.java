package com.flatshareteam.flatsharebackend.listing.dto;

import com.flatshareteam.flatsharebackend.listing.model.ListingStatus;

import java.util.UUID;

public record ListingStatusResponse(
        UUID listingId,
        ListingStatus status
) {
}
