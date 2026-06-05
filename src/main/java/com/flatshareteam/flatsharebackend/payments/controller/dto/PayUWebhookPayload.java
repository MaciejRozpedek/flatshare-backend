package com.flatshareteam.flatsharebackend.payments.controller.dto;

import lombok.Data;

@Data
public class PayUWebhookPayload {
    private Order order;

    @Data
    public static class Order {
        private String orderId;
        private String extOrderId;
        private String status;
    }
}
