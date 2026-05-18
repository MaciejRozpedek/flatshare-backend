package com.flatshareteam.flatsharebackend.payments.adapter.notification;

import com.flatshareteam.flatsharebackend.payments.event.PaymentStatusChangedEvent;
import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventNotificationAdapter implements IPaymentNotificationPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notifyPaymentStatusChanged(UUID bookingId, PaymentStatus newStatus) {
        log.info("Publishing PaymentStatusChangedEvent for booking {} to {}", bookingId, newStatus);
        eventPublisher.publishEvent(new PaymentStatusChangedEvent(this, bookingId, newStatus));
    }
}
