package com.flatshareteam.flatsharebackend.listings.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listings.dto.ListingStatusResponse;
import com.flatshareteam.flatsharebackend.listings.dto.*;
import com.flatshareteam.flatsharebackend.listings.service.DefaultListingService;
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

    private final DefaultListingService listingService;

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
            @AuthenticationPrincipal User authenticatedUser) {

        CreateListingResponse response = listingService.createListing(request, authenticatedUser.getId());

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
            @AuthenticationPrincipal User authenticatedUser) {

        ListingDto response = listingService.updateListing(listingId, request, authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{listingId}/publish")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ListingStatusResponse> publish(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingService.publish(listingId, authenticatedUser.getId()));
    }

    @PatchMapping("/{listingId}/hide")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ListingStatusResponse> hide(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingService.hide(listingId, authenticatedUser.getId()));
    }

    @PatchMapping("/{listingId}/archive")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ListingStatusResponse> archive(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingService.archive(listingId, authenticatedUser.getId()));
    }
}