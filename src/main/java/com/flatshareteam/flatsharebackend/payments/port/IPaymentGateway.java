package com.flatshareteam.flatsharebackend.payments.port;

import com.flatshareteam.flatsharebackend.payments.model.Payment;

public interface IPaymentGateway {
    /**
     * Initiates a payment process with the external gateway.
     *
     * @param payment the payment details
     * @return the redirect URL to the payment gateway page
     */
    String initiatePayment(Payment payment, com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationRequest request);
}
