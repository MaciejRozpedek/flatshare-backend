package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import com.flatshareteam.flatsharebackend.listings.repository.UnavailabilityRepository;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateResponse;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;
import com.flatshareteam.flatsharebackend.bookings.model.Booking;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
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
    private final ListingRepository listingRepository;
    private final TenantRoleRepository tenantRoleRepository;

    @Override
    @Transactional
    public BookingCreateResponse create(BookingCreateRequest request, UUID tenantUserId) {
        Listing listing = listingRepository.findFirstByRoomIdOrderByCreatedAtDesc(request.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found for this room"));

        TenantRole tenantRole = tenantRoleRepository.findByUserId(tenantUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant role not found"));

        if (!ListingStatus.ACTIVE.equals(listing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not active");
        }

        Booking booking = Booking.builder()
                .listing(listing)
                .tenantRole(tenantRole)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        assertDatesWithinListing(booking);
        assertAvailability(booking);

        Booking saved = bookingRepository.save(booking);

        BigDecimal totalPrice = listing.getPrice() != null ? listing.getPrice().getAmount() : BigDecimal.ZERO;
        String currency = listing.getPrice() != null ? listing.getPrice().getCurrency() : "PLN";

        return new BookingCreateResponse(
                saved.getId(),
                saved.getStatus() != null ? saved.getStatus() : BookingStatus.PENDING_APPROVAL,
                saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now(),
                totalPrice,
                currency,
                "/api/v1/bookings/" + saved.getId()
        );
    }

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

    @Override
    @Transactional
    public BookingStatusResponse cancel(UUID bookingId, UUID tenantUserId, String reason) {
        Booking booking = loadBooking(bookingId);
        
        if (booking.getTenantRole() == null || booking.getTenantRole().getUser() == null || !tenantUserId.equals(booking.getTenantRole().getUser().getId())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the tenant can cancel this booking");
        }
        
        if (booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.CANCELLED) {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already cancelled or rejected");
        }

        Instant cancelledAt = Instant.now();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setDecisionAt(cancelledAt);
        booking.setDecisionReason(reason);
        Booking saved = bookingRepository.save(booking);

        return new BookingStatusResponse(
                saved.getId(),
                saved.getStatus(),
                null,
                null,
                cancelledAt,
                saved.getDecisionReason()
        );
    }

    @Override
    public BookingStatusResponse getStatus(UUID bookingId, UUID userId) {
        Booking booking = loadBooking(bookingId);

        if (userId != null) {
            boolean isTenant = booking.getTenantRole() != null 
                    && booking.getTenantRole().getUser() != null 
                    && userId.equals(booking.getTenantRole().getUser().getId());
            boolean isLandlord = booking.getListing() != null 
                    && booking.getListing().getLandlordRole() != null 
                    && booking.getListing().getLandlordRole().getUser() != null 
                    && userId.equals(booking.getListing().getLandlordRole().getUser().getId());
            
            if (!isTenant && !isLandlord) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this booking");
            }
        }

        Instant acceptedAt = null;
        Instant paymentRequiredUntil = null;
        Instant rejectedAt = null;

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT || booking.getStatus() == BookingStatus.ACCEPTED) {
            acceptedAt = booking.getDecisionAt();
            if (acceptedAt != null) {
                paymentRequiredUntil = acceptedAt.plus(PAYMENT_GRACE_PERIOD);
            }
        } else if (booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.CANCELLED) {
            rejectedAt = booking.getDecisionAt();
        }

        return new BookingStatusResponse(
                booking.getId(),
                booking.getStatus(),
                acceptedAt,
                paymentRequiredUntil,
                rejectedAt,
                booking.getDecisionReason()
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

