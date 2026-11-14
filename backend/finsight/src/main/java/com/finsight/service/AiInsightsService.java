
package com.finsight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class AiInsightsService {

    private static final Logger logger =
            LoggerFactory.getLogger(AiInsightsService.class);

    private static final long CACHE_TTL_MILLIS =
            Duration.ofMinutes(30).toMillis();

    private final RestClient restClient;

    private final Map<String, CacheEntry> responseCache =
            new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(
                    com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                    true
            );

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

    private synchronized String getCachedResponse(
            String endpoint,
            Map<String, Object> request) {

        try {
            String requestKey = endpoint + ":" +
                    objectMapper.writeValueAsString(request);

            CacheEntry cachedEntry = responseCache.get(requestKey);

            if (cachedEntry != null) {
                long age = System.currentTimeMillis()
                        - cachedEntry.timestamp;

                if (age < CACHE_TTL_MILLIS) {
                    logger.info(
                            "Returning cached AI response for {}",
                            endpoint
                    );
                    return cachedEntry.response;
                }

                responseCache.remove(requestKey);
                logger.info("Cached response expired for {}", endpoint);
            }

            String response = restClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            responseCache.put(requestKey, new CacheEntry(response));

            logger.info("New AI response cached for {}", endpoint);

            return response;

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Failed to generate cache key.", ex
            );
        }
    }

    public String getFinancialSummary(Map<String, Object> request) {
        logger.info("Received financial summary request.");

        try {
            String response = getCachedResponse(
                    "/financial-summary", request
            );

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
            String response = getCachedResponse("/ask-ai", request);

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

    public Map<String, String> checkAiHealth() {
        logger.info("Checking Python AI service health.");

        try {
            restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();

            return Map.of("status", "UP");

        } catch (RestClientException ex) {
            logger.warn("Python AI service is unavailable: {}", ex.getMessage());
            return Map.of("status", "DOWN");
        }
    }

    private static class CacheEntry {

        private final String response;
        private final long timestamp;

        public CacheEntry(String response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
