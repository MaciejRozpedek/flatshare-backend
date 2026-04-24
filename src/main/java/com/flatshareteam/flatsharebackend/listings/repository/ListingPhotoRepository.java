package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, UUID> {
    List<ListingPhoto> findAllByListingId(UUID listingId);
    Optional<ListingPhoto> findByIdAndListingId(UUID photoId, UUID listingId);
}
