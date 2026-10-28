
package com.finsight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AiInsightsService {

    private final RestClient restClient;

    public AiInsightsService(
            @Value("${python.api.base-url:http://127.0.0.1:8000}")
            String pythonApiBaseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(pythonApiBaseUrl)
                .build();
    }

    public String getFinancialSummary(Map<String, Object> request) {
        return restClient.post()
                .uri("/financial-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public String askAi(Map<String, Object> request) {
        return restClient.post()
                .uri("/ask-ai")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }
}
