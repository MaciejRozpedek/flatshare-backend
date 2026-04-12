package com.flatshareteam.flatsharebackend.listing.service;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listing.dto.ListingStatusResponse;

import java.util.UUID;

public interface ListingStatusService {

    ListingStatusResponse publish(UUID listingId, User authenticatedUser);

    ListingStatusResponse hide(UUID listingId, User authenticatedUser);

    ListingStatusResponse archive(UUID listingId, User authenticatedUser);
}
