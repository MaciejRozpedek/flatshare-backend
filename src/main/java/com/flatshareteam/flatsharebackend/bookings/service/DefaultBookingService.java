package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import com.flatshareteam.flatsharebackend.listings.repository.UnavailabilityRepository;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;
import com.flatshareteam.flatsharebackend.bookings.model.Booking;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultBookingService implements BookingService {

    private static final Duration PAYMENT_GRACE_PERIOD = Duration.ofDays(1);

    private final BookingRepository bookingRepository;
    private final UnavailabilityRepository unavailabilityRepository;
    private final INotificationPort notificationPort;

    @Override
    @Transactional
    public BookingStatusResponse accept(UUID bookingId, UUID ownerUserId, String reason) {
        Booking booking = loadBooking(bookingId);
        assertOwner(booking, ownerUserId);
        assertPending(booking);
        assertDatesWithinListing(booking);
        assertAvailability(booking);

        Listing listing = booking.getListing();
        Unavailability unavailability = Unavailability.builder()
            .startDate(booking.getStartDate())
            .endDate(booking.getEndDate())
            .reason("BOOKING_ACCEPTED")
                .listing(listing)
                .build();
        unavailabilityRepository.save(unavailability);

        Instant acceptedAt = Instant.now();
        Instant paymentRequiredUntil = acceptedAt.plus(PAYMENT_GRACE_PERIOD);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setDecisionAt(acceptedAt);
        booking.setDecisionReason(reason);
        Booking saved = bookingRepository.save(booking);

        if (saved.getTenantRole() != null && saved.getTenantRole().getUser() != null) {
            notificationPort.notify(
                    saved.getTenantRole().getUser().getId(),
                    new NotificationData(
                            NotificationType.BOOKING_CONFIRMATION,
                            Map.of("bookingId", saved.getId().toString())
                    )
            );
        }

        return new BookingStatusResponse(
            saved.getId(),
            saved.getStatus(),
            acceptedAt,
            paymentRequiredUntil,
            null,
            null
        );
    }

    @Override
    @Transactional
    public BookingStatusResponse reject(UUID bookingId, UUID ownerUserId, String reason) {
        Booking booking = loadBooking(bookingId);
        assertOwner(booking, ownerUserId);
        assertPending(booking);

        Instant rejectedAt = Instant.now();
        booking.setStatus(BookingStatus.REJECTED);
        booking.setDecisionAt(rejectedAt);
        booking.setDecisionReason(reason);
        Booking saved = bookingRepository.save(booking);

        return new BookingStatusResponse(
                saved.getId(),
                saved.getStatus(),
                null,
                null,
                rejectedAt,
                saved.getDecisionReason()
        );
    }

    private Booking loadBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private void assertOwner(Booking booking, UUID ownerUserId) {
        if (booking.getListing() == null
                || booking.getListing().getLandlordRole() == null
                || booking.getListing().getLandlordRole().getUser() == null
                || !ownerUserId.equals(booking.getListing().getLandlordRole().getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the listing owner can decide on bookings");
        }
    }

    private void assertPending(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already decided");
        }
    }

    private void assertDatesWithinListing(Booking booking) {
        Listing listing = booking.getListing();
        if (listing == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is missing listing");
        }
        LocalDate start = booking.getStartDate();
        LocalDate end = booking.getEndDate();
        if (start == null || end == null || start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking dates");
        }
        if (start.isBefore(listing.getAvailableSince()) || end.isAfter(listing.getAvailableUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking dates outside listing availability");
        }
    }

    private void assertAvailability(Booking booking) {
        UUID listingId = booking.getListing().getId();
        boolean overlaps = unavailabilityRepository
                .existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        listingId,
                        booking.getEndDate(),
                        booking.getStartDate()
                );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing unavailable in selected dates");
        }
    }
}

