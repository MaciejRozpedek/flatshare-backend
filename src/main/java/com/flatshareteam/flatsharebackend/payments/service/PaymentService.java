package com.flatshareteam.flatsharebackend.payments.service;

import com.flatshareteam.flatsharebackend.payments.controller.dto.PayUWebhookPayload;
import com.flatshareteam.flatsharebackend.payments.model.Payment;
import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentNotificationPort;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentGateway;
import com.flatshareteam.flatsharebackend.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IPaymentGateway paymentGateway;
    private final IPaymentNotificationPort notificationPort;

    public record RefundInitiationResult(UUID paymentId, String refundReference) {
    }

    @Transactional
    public com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationResponse initiatePayment(UUID bookingId, BigDecimal amount, String currency, com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationRequest request) {
        log.info("Initiating payment for booking: {}, amount: {} {}", bookingId, amount, currency);

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(amount)
                .currency(currency)
                .status(PaymentStatus.INITIATED)
                .build();

        // Save initially to generate ID
        payment = paymentRepository.save(payment);

        try {
            String redirectUrl = paymentGateway.initiatePayment(payment, request);
            
            payment.setStatus(PaymentStatus.REDIRECTED);
            paymentRepository.save(payment);
            
            return com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationResponse.builder()
                    .paymentId(payment.getId())
                    .status(payment.getStatus())
                    .redirectUrl(redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initiate payment with gateway", e);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to initiate payment", e);
        }
    }

    @Transactional
    public void handleWebhook(PayUWebhookPayload payload) {
        if (payload == null || payload.getOrder() == null) {
            log.warn("Received empty or invalid webhook payload");
            return;
        }

        String orderId = payload.getOrder().getOrderId();
        String payUStatus = payload.getOrder().getStatus();
        
        log.info("Received webhook for PayU order ID: {}, status: {}", orderId, payUStatus);

        Optional<Payment> paymentOpt = paymentRepository.findByProviderReference(orderId);
        
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for provider reference: {}", orderId);
            return;
        }

        Payment payment = paymentOpt.get();
        PaymentStatus newStatus = mapPayUStatus(payUStatus);

        if (payment.getStatus() != newStatus) {
            payment.setStatus(newStatus);
            paymentRepository.save(payment);
            
            // Notify other modules asynchronously
            notificationPort.notifyPaymentStatusChanged(payment.getBookingId(), newStatus);
            
            log.info("Payment {} status updated to {}", payment.getId(), newStatus);
        } else {
            log.info("Payment {} status is already {}", payment.getId(), newStatus);
        }
    }

    private PaymentStatus mapPayUStatus(String payUStatus) {
        return switch (payUStatus.toUpperCase()) {
            case "COMPLETED" -> PaymentStatus.SUCCEEDED;
            case "CANCELED" -> PaymentStatus.CANCELLED;
            case "REJECTED" -> PaymentStatus.FAILED;
            case "PENDING", "WAITING_FOR_CONFIRMATION" -> PaymentStatus.INITIATED;
            default -> PaymentStatus.FAILED;
        };
    }

    public com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO getPaymentByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .max(java.util.Comparator.comparing(Payment::getCreatedAt))
                .map(this::mapToDTO)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Payment not found for booking"));
    }

    @Transactional
    public RefundInitiationResult initiateRefundForBooking(UUID bookingId, String reason) {
        Payment payment = paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                        bookingId,
                        PaymentStatus.SUCCEEDED
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Succeeded payment not found for booking"
                ));

        if (payment.getProviderReference() == null || payment.getProviderReference().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment provider reference is missing"
            );
        }

        String refundReference = paymentGateway.refundPayment(payment, reason);
        payment.setStatus(PaymentStatus.REFUND_PENDING);
        payment.setRefundReference(refundReference);
        payment.setRefundRequestedAt(Instant.now());
        paymentRepository.save(payment);

        return new RefundInitiationResult(payment.getId(), refundReference);
    }

    private com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO mapToDTO(Payment payment) {
        return com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .status(payment.getStatus())
                .totalValue(payment.getAmount())
                .currency(payment.getCurrency())
                .build();
    }
}
