package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import com.flatshareteam.flatsharebackend.listings.dto.ListingStatusResponse;
import com.flatshareteam.flatsharebackend.listings.dto.*;
import com.flatshareteam.flatsharebackend.listings.mapper.ListingMapper;
import com.flatshareteam.flatsharebackend.listings.model.*;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingSpecifications;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import com.flatshareteam.flatsharebackend.listings.repository.UnavailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultListingService implements ListingService {
    private final ListingRepository listingRepository;
    private final ApartmentRepository apartmentRepository;
    private final RoomRepository roomRepository;
    private final LandlordRoleRepository landlordRoleRepository;
    private final ListingMapper listingMapper;
    private final UnavailabilityRepository unavailabilityRepository;
    private final BookingRepository bookingRepository;

    private static final List<BookingStatus> COMMITTED_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING_PAYMENT,
            BookingStatus.ACCEPTED
    );

    @Override
    @Transactional
    public CreateListingResponse createListing(CreateListingRequest request, UUID userId) {
        String city = request.location().city();
        String district = request.location().district();
        String street = request.location().street();
        String aptNumber = request.location().aptNumber();

        Apartment apartment = apartmentRepository.findByCityAndDistrictAndStreetAndAptNumber(city, district, street, aptNumber)
                .orElseGet(() -> {
                    Apartment newApartment = Apartment.builder()
                            .city(city)
                            .district(district)
                            .street(street)
                            .aptNumber(aptNumber)
                            .build();
                    return apartmentRepository.save(newApartment);
                });

        Room room = Room.builder()
                .area(request.area().floatValue())
                .pricePerMonth(new Money(request.price(), request.currency()))
                .build();

        apartment.addRoom(room);
        room = roomRepository.save(room);

        LandlordRole landlordRole = landlordRoleRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Landlord role not found for the current user."));

        ListingAttributes attributes = ListingAttributes.builder()
                .petsAllowed(request.attributes().petsAllowed())
                .nonSmokingOnly(request.attributes().nonSmokingOnly())
                .closeToShops(request.attributes().closeToShops())
                .profile(request.attributes().profile())
                .build();

        Listing listing = Listing.builder()
                .title(request.title())
                .description(request.description())
                .price(new Money(request.price(), request.currency()))
                .status(ListingStatus.DRAFT)
                .createdAt(Instant.now())
                .availableSince(request.availableSince())
                .availableUntil(request.availableUntil())
                .ownerContact(request.ownerContact())
                .attributes(attributes)
                .landlordRole(landlordRole)
                .build();

        room.addListing(listing);
        listing = listingRepository.save(listing);

        return new CreateListingResponse(
                listing.getId().toString(),
                listing.getStatus(),
                listing.getCreatedAt()
        );
    }

    @Override
    public Page<ListingDto> getListings(ListingFilterCriteria criteria, Pageable pageable) {
        Specification<Listing> spec = ListingSpecifications.filterBy(criteria);
        return listingRepository.findAll(spec, pageable)
                .map(listingMapper::toDto);
    }

    @Override
    public ListingDto getListing(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        return listingMapper.toDto(listing);
    }

    @Override
    @Transactional
    public ListingDto updateListing(UUID listingId, UpdateListingRequest request, UUID userId) {
        Listing listing = loadOwnedListing(listingId, userId);

        Optional.ofNullable(request.availableSince()).ifPresent(listing::setAvailableSince);
        Optional.ofNullable(request.title()).ifPresent(listing::setTitle);
        Optional.ofNullable(request.description()).ifPresent(listing::setDescription);

        if (request.price() != null && request.currency() != null) {
            listing.setPrice(new Money(request.price(), request.currency()));
        } else if (request.price() != null || request.currency() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both price and currency must be provided together");
        }

        listing = listingRepository.save(listing);
        return listingMapper.toDto(listing);
    }

    @Override
    @Transactional
    public ListingStatusResponse publish(UUID listingId, UUID userId) {
        Listing listing = loadOwnedListing(listingId, userId);
        ListingStatus currentStatus = listing.getStatus();

        if (currentStatus == ListingStatus.ACTIVE) {
            return toResponse(listing);
        }

        if (currentStatus == ListingStatus.ARCHIVED || currentStatus == ListingStatus.HIDDEN_BY_MODERATION) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Listing cannot be published from status: " + currentStatus
            );
        }

        listing.setStatus(ListingStatus.ACTIVE);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    @Override
    @Transactional
    public ListingStatusResponse hide(UUID listingId, UUID userId) {
        Listing listing = loadOwnedListing(listingId, userId);
        ListingStatus currentStatus = listing.getStatus();

        if (currentStatus == ListingStatus.HIDDEN) {
            return toResponse(listing);
        }

        if (currentStatus != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active listings can be hidden"
            );
        }

        listing.setStatus(ListingStatus.HIDDEN);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    @Override
    @Transactional
    public ListingStatusResponse archive(UUID listingId, UUID userId) {
        Listing listing = loadOwnedListing(listingId, userId);

        if (listing.getStatus() == ListingStatus.ARCHIVED) {
            return toResponse(listing);
        }

        listing.setStatus(ListingStatus.ARCHIVED);
        Listing savedListing = listingRepository.save(listing);
        return toResponse(savedListing);
    }

    @Override
    @Transactional
    public void addUnavailability(UUID listingId, UnavailabilityRequest request, UUID userId) {
        Listing listing = loadOwnedListing(listingId, userId);

        assertValidUnavailabilityDates(request);
        assertNoOverlappingUnavailability(listingId, request);
        assertNoCommittedBookingOverlap(listingId, request);

        Unavailability unavailability = Unavailability.builder()
                .startDate(request.since())
                .endDate(request.until())
                .reason(request.message())
                .build();

        listing.addUnavailability(unavailability);
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public void removeUnavailability(UUID listingId, UUID unavailabilityId, UUID userId) {
        loadOwnedListing(listingId, userId);
        int deleted = unavailabilityRepository.deleteByIdAndListingId(unavailabilityId, listingId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unavailability not found");
        }
    }

    private Listing loadOwnedListing(UUID listingId, UUID userId) {
        return listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
    }

    private ListingStatusResponse toResponse(Listing listing) {
        return new ListingStatusResponse(listing.getId(), listing.getStatus());
    }

    private void assertValidUnavailabilityDates(UnavailabilityRequest request) {
        if (request.since().isAfter(request.until())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
        }
    }

    private void assertNoOverlappingUnavailability(UUID listingId, UnavailabilityRequest request) {
        boolean overlaps = unavailabilityRepository
                .existsByListingIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        listingId,
                        request.until(),
                        request.since()
                );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing already unavailable in selected dates");
        }
    }

    private void assertNoCommittedBookingOverlap(UUID listingId, UnavailabilityRequest request) {
        boolean overlaps = bookingRepository
                .existsByListingIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        listingId,
                        COMMITTED_BOOKING_STATUSES,
                        request.until(),
                        request.since()
                );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing has a committed booking in selected dates");
        }
    }
}
