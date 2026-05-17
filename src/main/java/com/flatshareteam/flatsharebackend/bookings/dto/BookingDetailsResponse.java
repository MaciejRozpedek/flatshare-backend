package com.flatshareteam.flatsharebackend.bookings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingDetailsResponse(
        UUID bookingId,
        UUID listingId,
        UUID tenantId,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice,
        String currency,
        String paymentStatus
) {}
