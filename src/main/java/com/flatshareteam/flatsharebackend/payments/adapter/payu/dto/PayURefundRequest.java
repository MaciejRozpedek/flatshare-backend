package com.flatshareteam.flatsharebackend.payments.adapter.payu.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayURefundRequest {
    private Refund refund;

    @Data
    @Builder
    public static class Refund {
        private String description;
        private String extRefundId;
        private String currencyCode;
        private String type;
    }
}
