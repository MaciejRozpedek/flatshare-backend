package com.flatshareteam.flatsharebackend.listings.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listings.dto.*;
import com.flatshareteam.flatsharebackend.listings.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingDto> getListing(@PathVariable UUID listingId) {
        ListingDto response = listingService.getListing(listingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ListingDto>> getListings(
            @ParameterObject ListingFilterCriteria criteria,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<ListingDto> response = listingService.getListings(criteria, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<CreateListingResponse> createListing(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal User currentUser) {

        CreateListingResponse response = listingService.createListing(request, currentUser.getId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.listingId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{listingId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ListingDto> updateListing(
            @PathVariable UUID listingId,
            @Valid @RequestBody UpdateListingRequest request,
            @AuthenticationPrincipal User currentUser) {

        ListingDto response = listingService.updateListing(listingId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}