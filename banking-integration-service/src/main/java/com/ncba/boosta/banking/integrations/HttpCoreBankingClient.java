package com.ncba.boosta.banking.integrations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpCoreBankingClient implements CoreBankingClient {
    private final WebClient client;
    private final String baseUrl;
    private final String apiKey;

    public HttpCoreBankingClient(
            WebClient.Builder builder,
            @Value("${integrations.core-banking.base-url:}") String baseUrl,
            @Value("${integrations.core-banking.api-key:}") String apiKey) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey;
        this.client = builder.baseUrl(this.baseUrl.isEmpty() ? "http://127.0.0.1" : this.baseUrl).build();
    }

    @Override
    public TransactionResult initiate(TransactionRequest request) {
        ensureConfigured();
        WebClient.RequestBodySpec call = client.post().uri("/transactions");
        if (apiKey != null && !apiKey.isBlank()) call.header("X-API-Key", apiKey);
        return call
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransactionResult.class)
                .blockOptional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Core banking returned an empty transaction response."));
    }

    @Override
    public TransactionResult getConfirmation(String transactionReference) {
        ensureConfigured();
        WebClient.RequestHeadersSpec<?> call =
                client.get().uri("/transactions/{reference}/confirmation", transactionReference);
        if (apiKey != null && !apiKey.isBlank()) call = call.header("X-API-Key", apiKey);
        return call
                .retrieve()
                .bodyToMono(TransactionResult.class)
                .blockOptional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Core banking returned an empty confirmation response."));
    }

    private void ensureConfigured() {
        if (baseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The core banking integration is not configured.");
        }
    }
}
