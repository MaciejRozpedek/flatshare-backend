package com.flatshareteam.flatsharebackend.payments.adapter.payu;

import com.flatshareteam.flatsharebackend.payments.adapter.payu.dto.PayUAuthResponse;
import com.flatshareteam.flatsharebackend.payments.adapter.payu.dto.PayUOrderRequest;
import com.flatshareteam.flatsharebackend.payments.adapter.payu.dto.PayUOrderResponse;
import com.flatshareteam.flatsharebackend.payments.adapter.payu.dto.PayURefundRequest;
import com.flatshareteam.flatsharebackend.payments.adapter.payu.dto.PayURefundResponse;
import com.flatshareteam.flatsharebackend.payments.model.Payment;
import com.flatshareteam.flatsharebackend.payments.port.IPaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayUAdapter implements IPaymentGateway {

    private final RestTemplate restTemplate;
    private final PayUProperties payUProperties;

    private String currentAccessToken;
    private long tokenExpiryTime;

    @Override
    public String initiatePayment(Payment payment, com.flatshareteam.flatsharebackend.payments.controller.dto.PaymentInitiationRequest req) {
        String token = getAccessToken();

        String continueUrl = (req != null && req.getReturnUrl() != null) ? req.getReturnUrl() : payUProperties.getContinueUrl();

        PayUOrderRequest.PayUOrderRequestBuilder requestBuilder = PayUOrderRequest.builder()
                .notifyUrl(payUProperties.getNotifyUrl())
                .continueUrl(continueUrl)
                .customerIp("127.0.0.1") // Typically retrieved from request context, hardcoded for simplicity
                .merchantPosId(payUProperties.getPosId())
                .description("FlatShare Booking " + payment.getBookingId())
                .currencyCode(payment.getCurrency())
                .totalAmount(payment.getAmount().movePointRight(2).toBigInteger().toString()) // PayU expects minor units (e.g., grosze)
                .extOrderId(payment.getId().toString())
                .products(List.of(
                        PayUOrderRequest.Product.builder()
                                .name("Booking Reservation")
                                .unitPrice(payment.getAmount().movePointRight(2).toBigInteger().toString())
                                .quantity("1")
                                .build()
                ));

        if (req != null && req.getPaymentMethod() != null && !req.getPaymentMethod().isBlank()) {
            requestBuilder.payMethods(PayUOrderRequest.PayMethods.builder()
                    .payMethod(PayUOrderRequest.PayMethod.builder()
                            .type("PBL")
                            .value(req.getPaymentMethod())
                            .build())
                    .build());
        }

        PayUOrderRequest requestBody = requestBuilder.build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PayUOrderRequest> request = new HttpEntity<>(requestBody, headers);
        
        String url = payUProperties.getBaseUrl() + "/api/v2_1/orders";

        log.info("Sending order request to PayU: {}", url);
        
        try {
            ResponseEntity<PayUOrderResponse> response = restTemplate.postForEntity(url, request, PayUOrderResponse.class);
            
            if ((response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) && response.getBody() != null) {
                PayUOrderResponse body = response.getBody();
                if ("SUCCESS".equals(body.getStatus().getStatusCode())) {
                    payment.setProviderReference(body.getOrderId());
                    return body.getRedirectUri();
                } else {
                    log.error("PayU returned non-success status: {}", body.getStatus().getStatusCode());
                    throw new RuntimeException("PayU order creation failed: " + body.getStatus().getStatusCode());
                }
            } else {
                throw new RuntimeException("Failed to initiate payment with PayU. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error communicating with PayU gateway", e);
            throw new RuntimeException("Payment Gateway Error", e);
        }
    }

    @Override
    public String refundPayment(Payment payment, String reason) {
        if (payment.getProviderReference() == null || payment.getProviderReference().isBlank()) {
            throw new IllegalArgumentException("Payment provider reference is required for refund");
        }

        String token = getAccessToken();
        PayURefundRequest requestBody = PayURefundRequest.builder()
                .refund(PayURefundRequest.Refund.builder()
                        .description(buildRefundDescription(payment, reason))
                        .extRefundId(payment.getId().toString())
                        .currencyCode(payment.getCurrency())
                        .type("REFUND_PAYMENT_STANDARD")
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PayURefundRequest> request = new HttpEntity<>(requestBody, headers);
        String url = payUProperties.getBaseUrl()
                + "/api/v2_1/orders/"
                + payment.getProviderReference()
                + "/refunds";

        log.info("Sending refund request to PayU: {}", url);

        try {
            ResponseEntity<PayURefundResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    PayURefundResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PayURefundResponse body = response.getBody();
                if (body.getStatus() != null && "SUCCESS".equals(body.getStatus().getStatusCode())) {
                    if (body.getRefund() != null && body.getRefund().getRefundId() != null) {
                        return body.getRefund().getRefundId();
                    }
                    return payment.getId().toString();
                }
                String status = body.getStatus() != null ? body.getStatus().getStatusCode() : "UNKNOWN";
                throw new RuntimeException("PayU refund failed: " + status);
            }

            throw new RuntimeException("Failed to initiate refund with PayU. Status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error communicating with PayU refund endpoint", e);
            throw new RuntimeException("Payment Gateway Refund Error", e);
        }
    }

    private String buildRefundDescription(Payment payment, String reason) {
        String normalizedReason = reason == null || reason.isBlank() ? "Moderation refund" : reason.trim();
        return "Refund for booking " + payment.getBookingId() + ": " + normalizedReason;
    }

    private synchronized String getAccessToken() {
        if (currentAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return currentAccessToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", payUProperties.getClientId());
        map.add("client_secret", payUProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String url = payUProperties.getBaseUrl() + "/pl/standard/user/oauth/authorize";

        log.info("Requesting new PayU access token");

        try {
            ResponseEntity<PayUAuthResponse> response = restTemplate.postForEntity(url, request, PayUAuthResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PayUAuthResponse authResponse = response.getBody();
                this.currentAccessToken = authResponse.getAccessToken();
                this.tokenExpiryTime = System.currentTimeMillis() + (authResponse.getExpiresIn() * 1000L) - 10000;
                return this.currentAccessToken;
            } else {
                throw new RuntimeException("Failed to get PayU access token. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching PayU access token", e);
            throw new RuntimeException("Payment Gateway Auth Error", e);
        }
    }
}
