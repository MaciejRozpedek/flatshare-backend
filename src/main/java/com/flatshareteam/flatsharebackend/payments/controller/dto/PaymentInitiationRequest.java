package com.flatshareteam.flatsharebackend.payments.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationRequest {
    private String paymentMethod;
    private String returnUrl;
    private String cancelUrl;
}
