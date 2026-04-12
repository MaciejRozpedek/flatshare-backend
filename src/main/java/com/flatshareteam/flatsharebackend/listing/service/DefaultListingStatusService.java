package com.flatshareteam.flatsharebackend.listing.service;

import com.flatshareteam.flatsharebackend.accounts.model.RoleType;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listing.dto.ListingStatusResponse;
import com.flatshareteam.flatsharebackend.listing.model.Listing;
import com.flatshareteam.flatsharebackend.listing.model.ListingStatus;
import com.flatshareteam.flatsharebackend.listing.repository.ListingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DefaultListingStatusService implements ListingStatusService {

    private final ListingRepository listingRepository;

    public DefaultListingStatusService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingStatusResponse publish(UUID listingId, User authenticatedUser) {
        Listing listing = loadOwnedListing(listingId, authenticatedUser);
        ListingStatus currentStatus = listing.getStatus();

        if (currentStatus == ListingStatus.ACTIVE) {
            return toResponse(listing);
        }

        if (currentStatus == ListingStatus.ARCHIVED || currentStatus == ListingStatus.HIDDEN_BY_MODERATION) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Listing cannot be published from status: " + currentStatus
            );
        }

        listing.setStatus(ListingStatus.ACTIVE);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    @Override
    @Transactional
    public ListingStatusResponse hide(UUID listingId, User authenticatedUser) {
        Listing listing = loadOwnedListing(listingId, authenticatedUser);
        ListingStatus currentStatus = listing.getStatus();

        if (currentStatus == ListingStatus.HIDDEN) {
            return toResponse(listing);
        }

        if (currentStatus != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active listings can be hidden"
            );
        }

        listing.setStatus(ListingStatus.HIDDEN);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    @Override
    @Transactional
    public ListingStatusResponse archive(UUID listingId, User authenticatedUser) {
        Listing listing = loadOwnedListing(listingId, authenticatedUser);

        if (listing.getStatus() == ListingStatus.ARCHIVED) {
            return toResponse(listing);
        }

        listing.setStatus(ListingStatus.ARCHIVED);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    private Listing loadOwnedListing(UUID listingId, User authenticatedUser) {
        validateLandlordRole(authenticatedUser);

        return listingRepository.findByIdAndLandlordRoleUserId(listingId, authenticatedUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
    }

    private void validateLandlordRole(User authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean hasLandlordRole = authenticatedUser.getRoles().stream()
                .anyMatch(role -> role.getRoleType() == RoleType.LANDLORD);

        if (!hasLandlordRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only landlords can manage listings");
        }
    }

    private ListingStatusResponse toResponse(Listing listing) {
        return new ListingStatusResponse(listing.getId(), listing.getStatus());
    }
}
