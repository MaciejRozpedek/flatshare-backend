package com.flatshareteam.flatsharebackend.bookings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingCancelResponse(
        UUID bookingId,
        String status,
        Instant cancelledAt,
        String refundStatus
) {}
