package com.flatshareteam.flatsharebackend.listing.repository;

import com.flatshareteam.flatsharebackend.listing.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    Optional<Listing> findByIdAndLandlordRoleUserId(UUID listingId, UUID userId);
}
