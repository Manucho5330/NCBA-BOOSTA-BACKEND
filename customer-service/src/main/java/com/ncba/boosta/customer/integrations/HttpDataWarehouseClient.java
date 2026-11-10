package com.ncba.boosta.customer.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpDataWarehouseClient implements DataWarehouseClient {
    private final WebClient client;
    private final String baseUrl;
    private final String apiKey;

    public HttpDataWarehouseClient(
            WebClient.Builder builder,
            @Value("${integrations.data-warehouse.base-url:}") String baseUrl,
            @Value("${integrations.data-warehouse.api-key:}") String apiKey) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey;
        this.client = builder.baseUrl(this.baseUrl.isEmpty() ? "http://127.0.0.1" : this.baseUrl).build();
    }

    @Override
    public JsonNode getCustomerProfile(String customerId) {
        if (baseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The customer data warehouse integration is not configured.");
        }
        WebClient.RequestHeadersSpec<?> call = client.get()
                .uri("/customers/{customerId}", customerId);
        if (apiKey != null && !apiKey.isBlank()) call = call.header("X-API-Key", apiKey);
        JsonNode profile = call.retrieve().bodyToMono(JsonNode.class).block();
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The customer data warehouse returned an empty response.");
        }
        return profile;
    }
}
