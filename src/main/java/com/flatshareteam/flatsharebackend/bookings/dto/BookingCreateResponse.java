package com.flatshareteam.flatsharebackend.bookings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingCreateResponse(
        UUID bookingId,
        BookingStatus status,
        Instant createdAt,
        BigDecimal totalPrice,
        String currency,
        String resourceLink
) {}
