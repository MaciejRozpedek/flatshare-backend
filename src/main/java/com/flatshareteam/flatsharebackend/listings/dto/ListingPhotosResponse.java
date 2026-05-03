package com.flatshareteam.flatsharebackend.listings.dto;

import java.util.List;
import java.util.UUID;

public record ListingPhotosResponse(
        UUID listingId,
        List<UUID> photos
) {}
