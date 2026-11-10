package com.ncba.boosta.portfolio.dto.request;

public record AnchorScoringRequest(String anchorId, String registrationNumber,
                                   long annualTurnoverKes, long requestedFacilityLimit) {
}
