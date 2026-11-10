package com.ncba.boosta.portfolio.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.ncba.boosta.portfolio.dto.request.AnchorScoringRequest;
import com.ncba.boosta.portfolio.dto.request.CustomerScoringRequest;

public interface ScoringIntegration {
    JsonNode scoreCustomer(CustomerScoringRequest request);
    JsonNode scoreAnchor(AnchorScoringRequest request);
    JsonNode getScore(String id);
}
