package com.ncba.boosta.customer.integrations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BankingIntegrationClient {
    private final WebClient client;

    public BankingIntegrationClient(@Qualifier("serviceWebClientBuilder") WebClient.Builder builder) {
        this.client = builder.baseUrl("http://banking-integration-service").build();
    }

    public TransactionResult initiate(TransactionRequest request, String authorization) {
        return client.post()
                .uri("/api/v1/banking/transactions")
                .header("Authorization", authorization)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransactionResult.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Banking service returned an empty transaction response."));
    }

    public TransactionResult confirmation(String reference, String authorization) {
        return client.get()
                .uri("/api/v1/banking/transactions/{reference}/confirmation", reference)
                .header("Authorization", authorization)
                .retrieve()
                .bodyToMono(TransactionResult.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Banking service returned an empty confirmation response."));
    }

    public record TransactionRequest(String customerId, String debitAccount, String creditAccount,
                                    long amount, String currency, String idempotencyKey, String narration) {
    }

    public record TransactionResult(String transactionReference, String status, String message,
                                    String confirmedAt) {
    }
}
