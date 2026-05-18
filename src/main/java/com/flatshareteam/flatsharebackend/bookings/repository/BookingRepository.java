package com.flatshareteam.flatsharebackend.bookings.repository;

import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByIdAndListingLandlordRoleUserId(UUID bookingId, UUID userId);

    List<Booking> findByTenantRoleUserId(UUID userId);

    List<Booking> findByListingLandlordRoleUserId(UUID userId);

    boolean existsByListingIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID listingId,
            Collection<BookingStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}

