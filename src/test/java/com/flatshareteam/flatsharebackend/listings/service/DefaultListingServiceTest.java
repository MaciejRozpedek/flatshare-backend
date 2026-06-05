package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import com.flatshareteam.flatsharebackend.listings.dto.UnavailabilityRequest;
import com.flatshareteam.flatsharebackend.listings.mapper.ListingMapper;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import com.flatshareteam.flatsharebackend.listings.model.Unavailability;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import com.flatshareteam.flatsharebackend.listings.repository.UnavailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private LandlordRoleRepository landlordRoleRepository;

    @Mock
    private ListingMapper listingMapper;

    @Mock
    private UnavailabilityRepository unavailabilityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private DefaultListingService listingService;

    @Test
    void addUnavailabilityAddsPeriodToOwnedListing() {
        UUID listingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate since = LocalDate.of(2026, 6, 1);
        LocalDate until = LocalDate.of(2026, 6, 30);
        Listing listing = listing(listingId);

        when(listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)).thenReturn(Optional.of(listing));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                listingId, until, since)).thenReturn(false);
        when(bookingRepository.existsByListingIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(listingId), anyCollection(), eq(until), eq(since))).thenReturn(false);
        when(listingRepository.save(listing)).thenReturn(listing);

        listingService.addUnavailability(listingId, new UnavailabilityRequest(since, until, "renovation"), userId);

        assertThat(listing.getUnavailabilities()).hasSize(1);
        Unavailability unavailability = listing.getUnavailabilities().getFirst();
        assertThat(unavailability.getStartDate()).isEqualTo(since);
        assertThat(unavailability.getEndDate()).isEqualTo(until);
        assertThat(unavailability.getReason()).isEqualTo("renovation");
        assertThat(unavailability.getListing()).isSameAs(listing);
        verify(listingRepository).save(listing);
    }

    @Test
    void addUnavailabilityRejectsInvalidDateRange() {
        UUID listingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Listing listing = listing(listingId);

        when(listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)).thenReturn(Optional.of(listing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> listingService.addUnavailability(
                        listingId,
                        new UnavailabilityRequest(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 1), null),
                        userId
                )
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(unavailabilityRepository, bookingRepository);
        verify(listingRepository, never()).save(listing);
    }

    @Test
    void addUnavailabilityRejectsOverlappingUnavailability() {
        UUID listingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate since = LocalDate.of(2026, 6, 1);
        LocalDate until = LocalDate.of(2026, 6, 30);
        Listing listing = listing(listingId);

        when(listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)).thenReturn(Optional.of(listing));
        when(unavailabilityRepository.existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                listingId, until, since)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> listingService.addUnavailability(
                        listingId,
                        new UnavailabilityRequest(since, until, null),
                        userId
                )
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(bookingRepository);
        verify(listingRepository, never()).save(listing);
    }

    @Test
    void removeUnavailabilityRemovesPeriodFromOwnedListing() {
        UUID listingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID unavailabilityId = UUID.randomUUID();
        Listing listing = listing(listingId);
        when(listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)).thenReturn(Optional.of(listing));
        when(unavailabilityRepository.deleteByIdAndListingId(unavailabilityId, listingId)).thenReturn(1);

        listingService.removeUnavailability(listingId, unavailabilityId, userId);

        verify(unavailabilityRepository).deleteByIdAndListingId(unavailabilityId, listingId);
        verify(listingRepository, never()).save(listing);
    }

    @Test
    void hideByModerationSetsStatusToHiddenByModeration() {
        UUID listingId = UUID.randomUUID();
        Listing listing = listing(listingId);
        listing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(listing)).thenReturn(listing);

        var response = listingService.hideByModeration(listingId);

        assertThat(response.listingId()).isEqualTo(listingId);
        assertThat(response.status()).isEqualTo(ListingStatus.HIDDEN_BY_MODERATION);
        verify(listingRepository).save(listing);
    }

    @Test
    void hideByModerationRejectsMissingListing() {
        UUID listingId = UUID.randomUUID();
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> listingService.hideByModeration(listingId)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(listingRepository, never()).save(any());
    }

    private Listing listing(UUID listingId) {
        Listing listing = new Listing();
        listing.setId(listingId);
        return listing;
    }
}
