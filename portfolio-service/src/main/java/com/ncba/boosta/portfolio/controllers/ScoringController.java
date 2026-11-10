package com.ncba.boosta.portfolio.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ncba.boosta.portfolio.services.ScoringIntegration;
import com.ncba.boosta.portfolio.dto.request.AnchorScoringRequest;
import com.ncba.boosta.portfolio.dto.request.CustomerScoringRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/scoring")
@Tag(name = "Scoring", description = "Customer and anchor scoring through the configured scoring provider")
public class ScoringController {
    private final ScoringIntegration scoring;

    public ScoringController(ScoringIntegration scoring) {
        this.scoring = scoring;
    }

    @PostMapping("/customers")
    @Operation(
            operationId = "scoreCustomer",
            summary = "Score a customer",
            description = "Submits the authenticated customer's scoring data to the configured scoring provider.")
    public JsonNode scoreCustomer(
            @RequestBody CustomerScoringRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer scoring data is required.");
        }
        requireOwnSubject(request.customerId(), jwt);
        return scoring.scoreCustomer(request);
    }

    @PostMapping("/anchors")
    @Operation(
            operationId = "scoreAnchor",
            summary = "Score an anchor",
            description = "Submits the authenticated anchor's scoring data to the configured scoring provider.")
    public JsonNode scoreAnchor(
            @RequestBody AnchorScoringRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anchor scoring data is required.");
        }
        requireOwnSubject(request.anchorId(), jwt);
        return scoring.scoreAnchor(request);
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "getScoringResult",
            summary = "Retrieve a scoring result",
            description = "Retrieves the result identified by the scoring engine ID. Access to the result is enforced by the scoring engine.")
    public JsonNode getScore(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A scoring result ID is required.");
        }
        return scoring.getScore(id);
    }

    private static void requireOwnSubject(String subjectId, Jwt jwt) {
        if (jwt == null || subjectId == null || !subjectId.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The scoring request must match the authenticated portal account.");
        }
    }
}
