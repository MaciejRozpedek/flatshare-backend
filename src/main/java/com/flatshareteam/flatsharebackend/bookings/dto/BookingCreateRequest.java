package com.flatshareteam.flatsharebackend.bookings.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BookingCreateRequest(
        UUID roomId,
        LocalDate startDate,
        LocalDate endDate
) {}
