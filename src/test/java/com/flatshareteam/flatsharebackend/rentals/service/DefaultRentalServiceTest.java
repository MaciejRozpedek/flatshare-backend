package com.flatshareteam.flatsharebackend.rentals.service;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
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
class DefaultRentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UnavailabilityRepository unavailabilityRepository;

    @Mock
    private INotificationPort notificationPort;

    @InjectMocks
    private DefaultRentalService rentalService;

    @Test
    void acceptPendingRentalCreatesUnavailabilityAndNotifiesTenant() {
        UUID rentalId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Rental rental = buildRental(rentalId, ownerId, tenantId, RentalStatus.PENDING_APPROVAL);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                rental.getListing().getId(), rental.getEndDate(), rental.getStartDate())).thenReturn(false);
        when(unavailabilityRepository.save(any(Unavailability.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalStatusResponse response = rentalService.accept(rentalId, ownerId, "ok");

        assertThat(response.status()).isEqualTo(RentalStatus.ACCEPTED);
        assertThat(rental.getDecisionAt()).isNotNull();
        assertThat(rental.getDecisionReason()).isEqualTo("ok");

        verify(unavailabilityRepository).save(any(Unavailability.class));
        ArgumentCaptor<NotificationData> dataCaptor = ArgumentCaptor.forClass(NotificationData.class);
        verify(notificationPort).notify(eq(tenantId), dataCaptor.capture());
        assertThat(dataCaptor.getValue().getType()).isEqualTo(NotificationType.BOOKING_CONFIRMATION);
    }

    @Test
    void rejectPendingRentalUpdatesStatusWithoutNotification() {
        UUID rentalId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Rental rental = buildRental(rentalId, ownerId, tenantId, RentalStatus.PENDING_APPROVAL);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalStatusResponse response = rentalService.reject(rentalId, ownerId, "no");

        assertThat(response.status()).isEqualTo(RentalStatus.REJECTED);
        verify(notificationPort, never()).notify(any(), any());
    }

    @Test
    void acceptThrowsWhenOwnerMismatch() {
        UUID rentalId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Rental rental = buildRental(rentalId, UUID.randomUUID(), tenantId, RentalStatus.PENDING_APPROVAL);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rentalService.accept(rentalId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void acceptThrowsWhenAlreadyDecided() {
        UUID rentalId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Rental rental = buildRental(rentalId, ownerId, tenantId, RentalStatus.ACCEPTED);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rentalService.accept(rentalId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void acceptThrowsWhenDatesOverlap() {
        UUID rentalId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Rental rental = buildRental(rentalId, ownerId, tenantId, RentalStatus.PENDING_APPROVAL);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                rental.getListing().getId(), rental.getEndDate(), rental.getStartDate())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rentalService.accept(rentalId, ownerId, null)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    private Rental buildRental(UUID rentalId, UUID ownerId, UUID tenantId, RentalStatus status) {
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

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setListing(listing);
        rental.setTenantRole(tenantRole);
        rental.setStartDate(LocalDate.of(2025, 1, 1));
        rental.setEndDate(LocalDate.of(2025, 6, 30));
        rental.setStatus(status);
        rental.setCreatedAt(Instant.now());
        return rental;
    }
}
