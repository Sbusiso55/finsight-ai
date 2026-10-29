
package com.finsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;

@Service
public class AiInsightsService {

    private static final Logger logger =
            LoggerFactory.getLogger(AiInsightsService.class);

    private final RestClient restClient;

    public AiInsightsService(
            @Value("${python.api.base-url:http://127.0.0.1:8000}")
            String pythonApiBaseUrl) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(30));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder()
                .baseUrl(pythonApiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String getFinancialSummary(Map<String, Object> request) {
        logger.info("Received financial summary request.");

        try {
            String response = restClient.post()
                    .uri("/financial-summary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            logger.info("Financial summary request completed successfully.");
            return response;

        } catch (RestClientException ex) {
            logger.error("Financial summary request failed: {}", ex.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI service is temporarily unavailable. Please try again later.",
                    ex
            );
        }
    }

    public String askAi(Map<String, Object> request) {
        logger.info("Received AI question request.");

        try {
            String response = restClient.post()
                    .uri("/ask-ai")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            logger.info("AI question request completed successfully.");
            return response;

        } catch (RestClientException ex) {
            logger.error("AI question request failed: {}", ex.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI service is temporarily unavailable. Please try again later.",
                    ex
            );
        }
    }
}
