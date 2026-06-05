package com.flatshareteam.flatsharebackend.payments.event;

import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class PaymentStatusChangedEvent extends ApplicationEvent {
    private final UUID bookingId;
    private final PaymentStatus status;

    public PaymentStatusChangedEvent(Object source, UUID bookingId, PaymentStatus status) {
        super(source);
        this.bookingId = bookingId;
        this.status = status;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
