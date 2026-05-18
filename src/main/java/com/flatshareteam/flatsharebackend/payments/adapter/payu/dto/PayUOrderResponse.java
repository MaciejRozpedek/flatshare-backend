package com.flatshareteam.flatsharebackend.payments.adapter.payu.dto;

import lombok.Data;

@Data
public class PayUOrderResponse {
    private Status status;
    private String redirectUri;
    private String orderId;
    private String extOrderId;

    @Data
    public static class Status {
        private String statusCode;
        private String statusDesc;
    }
}
