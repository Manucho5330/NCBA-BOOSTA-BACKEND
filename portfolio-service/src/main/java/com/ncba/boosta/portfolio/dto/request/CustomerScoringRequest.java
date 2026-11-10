package com.ncba.boosta.portfolio.dto.request;

public record CustomerScoringRequest(String customerId, String nationalId, String phone,
                                     String businessType, long requestedAmount) {
}
