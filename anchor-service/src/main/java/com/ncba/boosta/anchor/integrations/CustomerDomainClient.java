package com.ncba.boosta.anchor.integrations;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class CustomerDomainClient {
    private final WebClient client;

    public CustomerDomainClient(WebClient.Builder anchorWebClientBuilder) {
        this.client = anchorWebClientBuilder.baseUrl("http://customer-service").build();
    }

    public List<AnchorCustomer> customers(String anchorId, String authorization) {
        return client.get()
                .uri("/api/v1/anchor-data/{id}/customers", anchorId)
                .header("Authorization", authorization)
                .retrieve()
                .bodyToFlux(AnchorCustomer.class)
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Customer service returned no anchor customer data."));
    }

    public List<AnchorTransaction> transactions(String anchorId, String authorization) {
        return client.get()
                .uri("/api/v1/anchor-data/{id}/transactions", anchorId)
                .header("Authorization", authorization)
                .retrieve()
                .bodyToFlux(AnchorTransaction.class)
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Customer service returned no anchor transaction data."));
    }

    public record AnchorCustomer(String customerId, String name, String nationalId,
                                 long monthlySales, long annualSales, long limit, long outstanding,
                                 double utilisationPct, String overdueAgeing, int daysOverdue,
                                 String upcomingDueDate, String status) {
    }

    public record AnchorTransaction(String id, String reference, String date, String type,
                                    long amount, String status, String customerName) {
    }
}
