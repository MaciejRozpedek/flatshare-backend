package com.flatshareteam.flatsharebackend.payments.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {
    private com.flatshareteam.flatsharebackend.payments.model.PaymentStatus status;
    private java.util.UUID paymentId;
    private String redirectUrl;
}
