package com.flatshareteam.flatsharebackend.payments.adapter.payu.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PayUOrderRequest {
    private String notifyUrl;
    private String continueUrl;
    private String customerIp;
    private String merchantPosId;
    private String description;
    private String currencyCode;
    private String totalAmount;
    private String extOrderId;
    private List<Product> products;
    private Buyer buyer;
    private PayMethods payMethods;

    @Data
    @Builder
    public static class PayMethods {
        private PayMethod payMethod;
    }

    @Data
    @Builder
    public static class PayMethod {
        private String type;
        private String value;
    }

    @Data
    @Builder
    public static class Product {
        private String name;
        private String unitPrice;
        private String quantity;
    }

    @Data
    @Builder
    public static class Buyer {
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String language;
    }
}
