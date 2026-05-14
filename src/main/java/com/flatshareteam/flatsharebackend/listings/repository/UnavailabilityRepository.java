package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
