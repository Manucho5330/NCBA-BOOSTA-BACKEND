package com.ncba.boosta.customer.integrations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Component
public class ReconciliationTokenProvider {
    private final WebClient authClient;
    private final String identifier;
    private final String secret;
    private String token;
    private Instant expiresAt = Instant.MIN;

    public ReconciliationTokenProvider(
            @Qualifier("serviceWebClientBuilder") WebClient.Builder builder,
            @Value("${integrations.reconciliation.identifier:}") String identifier,
            @Value("${integrations.reconciliation.secret:}") String secret) {
        this.authClient = builder.baseUrl("http://auth-service").build();
        this.identifier = identifier;
        this.secret = secret;
    }

    public synchronized String getBearerToken() {
        if (token != null && Instant.now().plusSeconds(30).isBefore(expiresAt)) {
            return "Bearer " + token;
        }
        if (identifier.isBlank() || secret.isBlank()) {
            throw new IllegalStateException(
                    "Reconciliation BANK_CHECKER credentials are required to poll banking confirmations.");
        }
        LoginResult result = authClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(new LoginRequest(identifier, secret))
                .retrieve()
                .bodyToMono(LoginResult.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Auth service returned no reconciliation token."));
        if (!"BANK_CHECKER".equals(result.role()) || result.token() == null || result.token().isBlank()
                || result.expiresAt() == null) {
            throw new IllegalStateException("Reconciliation account must have the BANK_CHECKER role.");
        }
        token = result.token();
        expiresAt = result.expiresAt();
        return "Bearer " + token;
    }

    private record LoginRequest(String identifier, String secret) {
    }

    private record LoginResult(String token, String role, String subjectId, Instant expiresAt) {
    }
}
