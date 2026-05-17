package com.flatshareteam.flatsharebackend.bookings.model;

public enum BookingStatus {
    PENDING_APPROVAL,
    PENDING_PAYMENT,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    CONFIRMED,
    PAYMENT_FAILED,
    EXPIRED
}

