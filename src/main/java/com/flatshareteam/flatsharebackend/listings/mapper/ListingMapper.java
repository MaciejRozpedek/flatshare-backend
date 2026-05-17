package com.flatshareteam.flatsharebackend.listings.mapper;

import com.flatshareteam.flatsharebackend.listings.dto.ListingDto;
import com.flatshareteam.flatsharebackend.listings.model.Apartment;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingAttributes;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingDto toDto(Listing listing) {
        if (listing == null) {
            return null;
        }

        Double area = null;
        ListingDto.Location location = null;

        if (listing.getRoom() != null) {
            area = listing.getRoom().getArea() != null ? listing.getRoom().getArea().doubleValue() : null;
            if (listing.getRoom().getApartment() != null) {
                location = mapLocation(listing.getRoom().getApartment());
            }
        }

        return new ListingDto(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice() != null ? listing.getPrice().getAmount() : null,
                listing.getPrice() != null ? listing.getPrice().getCurrency() : null,
                listing.getAvailableSince(),
                listing.getAvailableUntil(),
                listing.getOwnerContact(),
                area,
                location,
                mapAttributes(listing.getAttributes()),
                listing.getStatus()
        );
    }

    private ListingDto.Location mapLocation(Apartment apartment) {
        if (apartment == null) {
            return null;
        }
        return new ListingDto.Location(
                apartment.getCity(),
                apartment.getDistrict(),
                apartment.getStreet(),
                apartment.getAptNumber()
        );
    }

    private ListingDto.Attributes mapAttributes(ListingAttributes attributes) {
        if (attributes == null) {
            return null;
        }
        return new ListingDto.Attributes(
                attributes.isPetsAllowed(),
                attributes.isNonSmokingOnly(),
                attributes.isCloseToShops(),
                attributes.getProfile()
        );
    }
}