package com.flatshareteam.flatsharebackend.rentals.dto;

import com.flatshareteam.flatsharebackend.rentals.model.RentalStatus;

import java.util.UUID;

public record RentalStatusResponse(UUID rentalId, RentalStatus status) {
}
