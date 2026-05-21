package com.flatshareteam.flatsharebackend.payments.port;

import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;

import java.util.UUID;

public interface IPaymentNotificationPort {
    /**
     * Notifies other modules about a payment status change.
     *
     * @param bookingId the ID of the booking associated with the payment
     * @param newStatus the new payment status
     */
    void notifyPaymentStatusChanged(UUID bookingId, PaymentStatus newStatus);
}
