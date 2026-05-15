package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UnavailabilityRepository unavailabilityRepository;

    @Mock
    private INotificationPort notificationPort;

    @InjectMocks
    private DefaultBookingService bookingService;

    @Test
    void acceptPendingBookingCreatesUnavailabilityAndNotifiesTenant() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Booking booking = buildBooking(bookingId, ownerId, tenantId, BookingStatus.PENDING_APPROVAL);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                booking.getListing().getId(), booking.getEndDate(), booking.getStartDate())).thenReturn(false);
        when(unavailabilityRepository.save(any(Unavailability.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingStatusResponse response = bookingService.accept(bookingId, ownerId, "ok");

        assertThat(response.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.getDecisionAt()).isNotNull();
        assertThat(booking.getDecisionReason()).isEqualTo("ok");

        verify(unavailabilityRepository).save(any(Unavailability.class));
        ArgumentCaptor<NotificationData> dataCaptor = ArgumentCaptor.forClass(NotificationData.class);
        verify(notificationPort).notify(eq(tenantId), dataCaptor.capture());
        assertThat(dataCaptor.getValue().getType()).isEqualTo(NotificationType.BOOKING_CONFIRMATION);
    }

    @Test
    void rejectPendingBookingUpdatesStatusWithoutNotification() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Booking booking = buildBooking(bookingId, ownerId, tenantId, BookingStatus.PENDING_APPROVAL);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingStatusResponse response = bookingService.reject(bookingId, ownerId, "no");

        assertThat(response.status()).isEqualTo(BookingStatus.REJECTED);
        verify(notificationPort, never()).notify(any(), any());
    }

    @Test
    void acceptThrowsWhenOwnerMismatch() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Booking booking = buildBooking(bookingId, UUID.randomUUID(), tenantId, BookingStatus.PENDING_APPROVAL);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.accept(bookingId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void acceptThrowsWhenAlreadyDecided() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Booking booking = buildBooking(bookingId, ownerId, tenantId, BookingStatus.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.accept(bookingId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void acceptThrowsWhenDatesOverlap() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Booking booking = buildBooking(bookingId, ownerId, tenantId, BookingStatus.PENDING_APPROVAL);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            booking.getListing().getId(), booking.getEndDate(), booking.getStartDate())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.accept(bookingId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    private Booking buildBooking(UUID bookingId, UUID ownerId, UUID tenantId, BookingStatus status) {
        User owner = new User();
        owner.setId(ownerId);
        LandlordRole landlordRole = new LandlordRole();
        landlordRole.setUser(owner);

        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setLandlordRole(landlordRole);
        listing.setAvailableSince(LocalDate.of(2024, 1, 1));
        listing.setAvailableUntil(LocalDate.of(2026, 12, 31));

        User tenant = new User();
        tenant.setId(tenantId);
        TenantRole tenantRole = new TenantRole();
        tenantRole.setUser(tenant);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setListing(listing);
        booking.setTenantRole(tenantRole);
        booking.setStartDate(LocalDate.of(2025, 1, 1));
        booking.setEndDate(LocalDate.of(2025, 6, 30));
        booking.setStatus(status);
        booking.setCreatedAt(Instant.now());
        return booking;
    }
}

