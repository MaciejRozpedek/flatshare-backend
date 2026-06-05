package com.flatshareteam.flatsharebackend.payments.adapter.payu.dto;

import lombok.Data;

@Data
public class PayURefundResponse {
    private String orderId;
    private Refund refund;
    private Status status;

    @Data
    public static class Refund {
        private String refundId;
        private String extRefundId;
        private String amount;
        private String currencyCode;
        private String status;
    }

    @Data
    public static class Status {
        private String statusCode;
        private String statusDesc;
    }
}
