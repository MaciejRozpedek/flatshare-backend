package com.flatshareteam.flatsharebackend.payments.controller.dto;

import com.flatshareteam.flatsharebackend.payments.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID paymentId;
    private UUID bookingId;
    private PaymentStatus status;
    private BigDecimal totalValue;
    private String currency;
}
