package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCancelResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingDetailsResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;

import java.util.UUID;

public interface BookingService {
    BookingCreateResponse create(BookingCreateRequest request, UUID tenantUserId);
    BookingStatusResponse accept(UUID bookingId, UUID ownerUserId, String reason);
    BookingStatusResponse reject(UUID bookingId, UUID ownerUserId, String reason);
    BookingCancelResponse cancel(UUID bookingId, UUID tenantUserId, String reason);
    BookingDetailsResponse getStatus(UUID bookingId, UUID userId);
}

