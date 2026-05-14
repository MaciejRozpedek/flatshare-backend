package com.flatshareteam.flatsharebackend.rentals.service;

import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import com.flatshareteam.flatsharebackend.listings.repository.UnavailabilityRepository;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import com.flatshareteam.flatsharebackend.rentals.dto.RentalStatusResponse;
import com.flatshareteam.flatsharebackend.rentals.model.Rental;
import com.flatshareteam.flatsharebackend.rentals.model.RentalStatus;
import com.flatshareteam.flatsharebackend.rentals.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultRentalService implements RentalService {

    private final RentalRepository rentalRepository;
    private final UnavailabilityRepository unavailabilityRepository;
    private final INotificationPort notificationPort;

    @Override
    @Transactional
    public RentalStatusResponse accept(UUID rentalId, UUID ownerUserId, String reason) {
        Rental rental = loadRental(rentalId);
        assertOwner(rental, ownerUserId);
        assertPending(rental);
        assertDatesWithinListing(rental);
        assertAvailability(rental);

        Listing listing = rental.getListing();
        Unavailability unavailability = Unavailability.builder()
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .reason("RENTAL_ACCEPTED")
                .listing(listing)
                .build();
        unavailabilityRepository.save(unavailability);

        rental.setStatus(RentalStatus.ACCEPTED);
        rental.setDecisionAt(Instant.now());
        rental.setDecisionReason(reason);
        Rental saved = rentalRepository.save(rental);

        if (saved.getTenantRole() != null && saved.getTenantRole().getUser() != null) {
            notificationPort.notify(
                    saved.getTenantRole().getUser().getId(),
                    new NotificationData(
                            NotificationType.BOOKING_CONFIRMATION,
                            Map.of("bookingId", saved.getId().toString())
                    )
            );
        }

        return new RentalStatusResponse(saved.getId(), saved.getStatus());
    }

    @Override
    @Transactional
    public RentalStatusResponse reject(UUID rentalId, UUID ownerUserId, String reason) {
        Rental rental = loadRental(rentalId);
        assertOwner(rental, ownerUserId);
        assertPending(rental);

        rental.setStatus(RentalStatus.REJECTED);
        rental.setDecisionAt(Instant.now());
        rental.setDecisionReason(reason);
        Rental saved = rentalRepository.save(rental);

        return new RentalStatusResponse(saved.getId(), saved.getStatus());
    }

    private Rental loadRental(UUID rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
    }

    private void assertOwner(Rental rental, UUID ownerUserId) {
        if (rental.getListing() == null
                || rental.getListing().getLandlordRole() == null
                || rental.getListing().getLandlordRole().getUser() == null
                || !ownerUserId.equals(rental.getListing().getLandlordRole().getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the listing owner can decide on rentals");
        }
    }

    private void assertPending(Rental rental) {
        if (rental.getStatus() != RentalStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental already decided");
        }
    }

    private void assertDatesWithinListing(Rental rental) {
        Listing listing = rental.getListing();
        if (listing == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental is missing listing");
        }
        LocalDate start = rental.getStartDate();
        LocalDate end = rental.getEndDate();
        if (start == null || end == null || start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rental dates");
        }
        if (start.isBefore(listing.getAvailableSince()) || end.isAfter(listing.getAvailableUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental dates outside listing availability");
        }
    }

    private void assertAvailability(Rental rental) {
        UUID listingId = rental.getListing().getId();
        boolean overlaps = unavailabilityRepository
                .existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        listingId,
                        rental.getEndDate(),
                        rental.getStartDate()
                );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing unavailable in selected dates");
        }
    }
}
