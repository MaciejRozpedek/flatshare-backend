package com.flatshareteam.flatsharebackend.rentals.service;

import com.flatshareteam.flatsharebackend.rentals.dto.RentalStatusResponse;

import java.util.UUID;

public interface RentalService {
    RentalStatusResponse accept(UUID rentalId, UUID ownerUserId, String reason);
    RentalStatusResponse reject(UUID rentalId, UUID ownerUserId, String reason);
}
