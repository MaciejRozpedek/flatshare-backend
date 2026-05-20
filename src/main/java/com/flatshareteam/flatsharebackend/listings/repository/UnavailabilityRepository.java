package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface UnavailabilityRepository extends JpaRepository<Unavailability, UUID> {
    boolean existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID listingId,
            LocalDate endDate,
            LocalDate startDate
    );
    void deleteByListingIdAndStartDateAndEndDate(UUID listingId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("delete from Unavailability u where u.id = :id and u.listing.id = :listingId")
    int deleteByIdAndListingId(UUID id, UUID listingId);
}
