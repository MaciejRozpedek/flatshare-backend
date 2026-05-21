package com.flatshareteam.flatsharebackend.bookings.service;

import com.flatshareteam.flatsharebackend.payments.event.PaymentStatusChangedEvent;
import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingPaymentListener {

    private final BookingService bookingService;

    @EventListener
    public void handlePaymentStatusChangedEvent(PaymentStatusChangedEvent event) {
        log.info("Received PaymentStatusChangedEvent for booking {} with status {}", event.getBookingId(), event.getStatus());
        
        if (event.getStatus() == PaymentStatus.SUCCEEDED) {
            bookingService.confirmPayment(event.getBookingId());
        } else if (event.getStatus() == PaymentStatus.FAILED || event.getStatus() == PaymentStatus.CANCELLED) {
            bookingService.failPayment(event.getBookingId());
        }
    }
}
