package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;

import java.util.UUID;

public interface BookingService {
    BookingStatusResponse accept(UUID bookingId, UUID ownerUserId, String reason);
    BookingStatusResponse reject(UUID bookingId, UUID ownerUserId, String reason);
}

