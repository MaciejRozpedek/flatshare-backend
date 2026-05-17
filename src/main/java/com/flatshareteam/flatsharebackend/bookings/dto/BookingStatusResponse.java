package com.flatshareteam.flatsharebackend.bookings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingStatusResponse(
	UUID bookingId,
	BookingStatus status,
	Instant acceptedAt,
	Instant paymentRequiredUntil,
	Instant rejectedAt,
	String reason
) {
}

