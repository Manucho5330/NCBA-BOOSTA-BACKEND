package com.ncba.boosta.portfolio.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ncba.boosta.portfolio.dto.request.AnchorScoringRequest;
import com.ncba.boosta.portfolio.dto.request.CustomerScoringRequest;
import com.ncba.boosta.portfolio.services.ScoringIntegration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpScoringIntegration implements ScoringIntegration {
    private final WebClient client;
    private final String baseUrl;
    private final String apiKey;

    public HttpScoringIntegration(
            WebClient.Builder builder,
            @Value("${integrations.scoring.base-url:}") String baseUrl,
            @Value("${integrations.scoring.api-key:}") String apiKey) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey;
        this.client = builder.baseUrl(this.baseUrl.isEmpty() ? "http://127.0.0.1" : this.baseUrl).build();
    }

    @Override
    public JsonNode scoreCustomer(CustomerScoringRequest request) {
        return post("/scores/customer", request);
    }

    @Override
    public JsonNode scoreAnchor(AnchorScoringRequest request) {
        return post("/scores/anchor", request);
    }

    @Override
    public JsonNode getScore(String id) {
        if (baseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The scoring engine integration is not configured.");
        }
        WebClient.RequestHeadersSpec<?> call = client.get().uri("/scores/{id}", id);
        if (apiKey != null && !apiKey.isBlank()) call.header("X-API-Key", apiKey);
        try {
            JsonNode result = call.retrieve().bodyToMono(JsonNode.class).block();
            if (result == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The scoring engine returned an empty response.");
            }
            return result;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The scoring engine request failed.", exception);
        }
    }

    private JsonNode post(String path, Object request) {
        if (baseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The scoring engine integration is not configured.");
        }
        WebClient.RequestBodySpec call = client.post().uri(path);
        if (apiKey != null && !apiKey.isBlank()) call.header("X-API-Key", apiKey);
        try {
            JsonNode result = call.bodyValue(request).retrieve().bodyToMono(JsonNode.class).block();
            if (result == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The scoring engine returned an empty response.");
            }
            return result;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The scoring engine request failed.", exception);
        }
    }
}
