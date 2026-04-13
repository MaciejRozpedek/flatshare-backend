package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.listings.dto.*;
import com.flatshareteam.flatsharebackend.listings.mapper.ListingMapper;
import com.flatshareteam.flatsharebackend.listings.model.*;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingSpecifications;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListingService {
    private final ListingRepository listingRepository;
    private final ApartmentRepository apartmentRepository;
    private final RoomRepository roomRepository;
    private final LandlordRoleRepository landlordRoleRepository;
    private final ListingMapper listingMapper;

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
    
    public Page<ListingDto> getListings(ListingFilterCriteria criteria, Pageable pageable) {
        Specification<Listing> spec = ListingSpecifications.filterBy(criteria);
        return listingRepository.findAll(spec, pageable)
                .map(listingMapper::toDto);
    }

    public ListingDto getListing(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        return listingMapper.toDto(listing);
    }

    @Transactional
    public ListingDto updateListing(UUID listingId, UpdateListingRequest request, UUID userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        if (!listing.getLandlordRole().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this listing");
        }

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
}