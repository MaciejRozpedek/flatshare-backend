package com.flatshareteam.flatsharebackend.payments.controller;

import com.flatshareteam.flatsharebackend.payments.controller.dto.PayUWebhookPayload;
import com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationResponse;
import com.flatshareteam.flatsharebackend.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.flatshareteam.flatsharebackend.bookings.service.BookingService;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @PathVariable UUID bookingId,
            @RequestBody(required = false) com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationRequest request) {
        log.info("REST request to initiate payment for booking: {}", bookingId);
        
        BookingDetailsResponse bookingDetails = bookingService.getStatus(bookingId, null);
        
        PaymentInitiationResponse response = paymentService.initiatePayment(bookingId, bookingDetails.totalPrice(), bookingDetails.currency(), request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO> getPayment(@PathVariable UUID paymentId) {
        log.info("REST request to get payment: {}", paymentId);
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @GetMapping("/payments")
    public ResponseEntity<com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentDTO> getPaymentByBooking(@RequestParam UUID bookingId) {
        log.info("REST request to get payment for booking: {}", bookingId);
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
    }

    @PostMapping("/payments/payu-webhook")
    public ResponseEntity<Void> handlePayUWebhook(@RequestBody PayUWebhookPayload payload) {
        log.info("REST request to handle PayU webhook");
        
        paymentService.handleWebhook(payload);
        
        // PayU expects 200 OK
        return ResponseEntity.ok().build();
    }
}
