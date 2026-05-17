package com.flatshareteam.flatsharebackend.bookings.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCancelResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingDetailsResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingDecisionRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;
import com.flatshareteam.flatsharebackend.bookings.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<BookingCreateResponse> create(
            @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        BookingCreateResponse response = bookingService.create(request, authenticatedUser.getId());
        URI location = URI.create("/api/v1/bookings/" + response.bookingId());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{bookingId}/accept")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<BookingStatusResponse> accept(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(required = false) BookingDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        BookingStatusResponse response = bookingService.accept(bookingId, authenticatedUser.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/reject")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<BookingStatusResponse> reject(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(required = false) BookingDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        BookingStatusResponse response = bookingService.reject(bookingId, authenticatedUser.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<BookingCancelResponse> cancel(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(required = false) BookingDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        BookingCancelResponse response = bookingService.cancel(bookingId, authenticatedUser.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailsResponse> getStatus(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        UUID userId = authenticatedUser != null ? authenticatedUser.getId() : null;
        BookingDetailsResponse response = bookingService.getStatus(bookingId, userId);
        return ResponseEntity.ok(response);
    }
}

