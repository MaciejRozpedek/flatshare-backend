package com.flatshareteam.flatsharebackend.rentals.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.rentals.dto.RentalDecisionRequest;
import com.flatshareteam.flatsharebackend.rentals.dto.RentalStatusResponse;
import com.flatshareteam.flatsharebackend.rentals.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/{rentalId}/accept")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<RentalStatusResponse> accept(
            @PathVariable UUID rentalId,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(required = false) RentalDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        RentalStatusResponse response = rentalService.accept(rentalId, authenticatedUser.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rentalId}/reject")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<RentalStatusResponse> reject(
            @PathVariable UUID rentalId,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(required = false) RentalDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        RentalStatusResponse response = rentalService.reject(rentalId, authenticatedUser.getId(), reason);
        return ResponseEntity.ok(response);
    }
}
