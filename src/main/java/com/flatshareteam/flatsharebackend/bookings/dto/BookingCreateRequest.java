package com.flatshareteam.flatsharebackend.bookings.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BookingCreateRequest(
        UUID listingId,
        LocalDate startDate,
        LocalDate endDate
) {}
