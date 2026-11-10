package com.ncba.boosta.customer.integrations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class AnchorServiceClient {
    private final WebClient client;

    public AnchorServiceClient(@Qualifier("serviceWebClientBuilder") WebClient.Builder builder) {
        this.client = builder.baseUrl("http://anchor-service").build();
    }

    public AnchorRecord getAnchor(String anchorId, String authorization) {
        return client.get()
                .uri("/api/v1/anchors/{id}/disbursement-details", anchorId)
                .header("Authorization", authorization)
                .retrieve()
                .bodyToMono(AnchorRecord.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Anchor service returned an empty anchor response."));
    }

    public long countAnchors(String authorization) {
        AnchorSummary[] anchors = client.get()
                .uri("/api/v1/anchors")
                .header("Authorization", authorization)
                .retrieve()
                .bodyToMono(AnchorSummary[].class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Anchor service returned no anchor list."));
        return anchors.length;
    }

    public void reserve(String anchorId, String reservationId, long amount, String authorization) {
        client.post()
                .uri("/api/v1/anchors/{id}/reservations", anchorId)
                .header("Authorization", authorization)
                .bodyValue(new ReservationRequest(reservationId, amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void release(String anchorId, String reservationId, String authorization) {
        client.post()
                .uri("/api/v1/anchors/{id}/reservations/{reservationId}/release", anchorId, reservationId)
                .header("Authorization", authorization)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void creditLimit(String anchorId, String adjustmentId, long amount, String authorization) {
        client.post()
                .uri("/api/v1/anchors/{id}/limit-adjustments", anchorId)
                .header("Authorization", authorization)
                .bodyValue(new LimitAdjustmentRequest(adjustmentId, amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public record AnchorRecord(String id, boolean drawdownAllowed,
                               String disbursementAccountNumber, String collectionAccountNumber,
                               List<Integer> allowedTenors) {
        public boolean allowsTenor(int tenorDays) {
            return allowedTenors == null || allowedTenors.contains(tenorDays);
        }
    }

    private record AnchorSummary(String id) {
    }

    private record ReservationRequest(String reservationId, long amount) {
    }

    private record LimitAdjustmentRequest(String adjustmentId, long amount) {
    }
}
