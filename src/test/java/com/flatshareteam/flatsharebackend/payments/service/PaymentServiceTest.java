package com.flatshareteam.flatsharebackend.payments.service;

import com.flatshareteam.flatsharebackend.payments.model.Payment;
import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentGateway;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentNotificationPort;
import com.flatshareteam.flatsharebackend.payments.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IPaymentGateway paymentGateway;

    @Mock
    private IPaymentNotificationPort notificationPort;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void initiateRefundForBookingCallsGatewayAndMarksPaymentAsRefundPending() {
        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(paymentId)
                .bookingId(bookingId)
                .status(PaymentStatus.SUCCEEDED)
                .providerReference("payu-order-1")
                .amount(BigDecimal.valueOf(2000))
                .currency("PLN")
                .build();

        when(paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                bookingId,
                PaymentStatus.SUCCEEDED
        )).thenReturn(Optional.of(payment));
        when(paymentGateway.refundPayment(payment, "moderation")).thenReturn("refund-1");

        PaymentService.RefundInitiationResult result =
                paymentService.initiateRefundForBooking(bookingId, "moderation");

        assertThat(result.paymentId()).isEqualTo(paymentId);
        assertThat(result.refundReference()).isEqualTo("refund-1");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
        assertThat(payment.getRefundReference()).isEqualTo("refund-1");
        assertThat(payment.getRefundRequestedAt()).isNotNull();

        verify(paymentGateway).refundPayment(payment, "moderation");
        verify(paymentRepository).save(payment);
    }
}
