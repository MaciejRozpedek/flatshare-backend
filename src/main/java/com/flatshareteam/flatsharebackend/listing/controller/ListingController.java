package com.flatshareteam.flatsharebackend.listing.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listing.dto.ListingStatusResponse;
import com.flatshareteam.flatsharebackend.listing.service.ListingStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingStatusService listingStatusService;

    public ListingController(ListingStatusService listingStatusService) {
        this.listingStatusService = listingStatusService;
    }

    @PatchMapping("/{listingId}/publish")
    public ResponseEntity<ListingStatusResponse> publish(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingStatusService.publish(listingId, authenticatedUser));
    }

    @PatchMapping("/{listingId}/hide")
    public ResponseEntity<ListingStatusResponse> hide(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingStatusService.hide(listingId, authenticatedUser));
    }

    @PatchMapping("/{listingId}/archive")
    public ResponseEntity<ListingStatusResponse> archive(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(listingStatusService.archive(listingId, authenticatedUser));
    }
}
