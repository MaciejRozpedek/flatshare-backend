package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
    Optional<Listing> findByIdAndLandlordRoleUserId(UUID listingId, UUID userId);

    List<Listing> findByLandlordRoleUserIdAndStatus(UUID userId, ListingStatus status);
}
