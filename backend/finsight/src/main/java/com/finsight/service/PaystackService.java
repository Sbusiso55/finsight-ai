package com.finsight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class PaystackService {

    private final RestClient client;
    private final String secretKey;

    public PaystackService(@Value("${PAYSTACK_SECRET_KEY:}") String secretKey) {
        this.secretKey = secretKey;
        this.client = RestClient.builder().baseUrl("https://api.paystack.co").build();
    }

    private void requireKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payments are not set up yet");
        }
    }

    @SuppressWarnings("unchecked")
    public String initialize(String email, long amountCents, String reference, String callbackUrl,
                             int months, Long userId) {
        requireKey();
        Map<String, Object> body = Map.of(
                "email", email,
                "amount", amountCents,
                "currency", "ZAR",
                "reference", reference,
                "callback_url", callbackUrl,
                "metadata", Map.of("userId", userId, "months", months)
        );
        try {
            Map<String, Object> res = client.post()
                    .uri("/transaction/initialize")
                    .header("Authorization", "Bearer " + secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> data = res == null ? null : (Map<String, Object>) res.get("data");
            if (data == null || data.get("authorization_url") == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not start payment");
            }
            return (String) data.get("authorization_url");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach the payment provider");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verify(String reference) {
        requireKey();
        try {
            Map<String, Object> res = client.get()
                    .uri("/transaction/verify/{reference}", reference)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> data = res == null ? null : (Map<String, Object>) res.get("data");
            if (data == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not verify payment");
            }
            return data;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach the payment provider");
        }
    }
}