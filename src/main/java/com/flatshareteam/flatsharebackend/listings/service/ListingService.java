package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listings.dto.CreateListingRequest;
import com.flatshareteam.flatsharebackend.listings.dto.CreateListingResponse;
import com.flatshareteam.flatsharebackend.listings.dto.ListingDto;
import com.flatshareteam.flatsharebackend.listings.dto.ListingFilterCriteria;
import com.flatshareteam.flatsharebackend.listings.dto.ListingStatusResponse;
import com.flatshareteam.flatsharebackend.listings.dto.UpdateListingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListingService {
    CreateListingResponse createListing(CreateListingRequest request, UUID userId);
    Page<ListingDto> getListings(ListingFilterCriteria criteria, Pageable pageable);
    ListingDto getListing(UUID listingId);
    ListingDto updateListing(UUID listingId, UpdateListingRequest request, UUID userId);
    ListingStatusResponse publish(UUID listingId, UUID userId);
    ListingStatusResponse hide(UUID listingId, UUID userId);
    ListingStatusResponse archive(UUID listingId, UUID userId);
}