package com.ncba.boosta.anchor.controllers;

import com.ncba.boosta.anchor.models.AnchorOnboardingStep;
import com.ncba.boosta.anchor.dto.request.CreateAnchorOnboardingRequest;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest;
import com.ncba.boosta.anchor.dto.response.AnchorOnboardingResponse;
import com.ncba.boosta.anchor.services.AnchorOnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anchor-onboardings")
public class AnchorOnboardingController {
    private final AnchorOnboardingService service;

    public AnchorOnboardingController(AnchorOnboardingService service) {
        this.service = service;
    }

    @GetMapping
    public List<AnchorOnboardingResponse> list(@AuthenticationPrincipal Jwt jwt) {
        String actorId = actorId(jwt);
        return service.list(actorId, reviewer(jwt)).stream().map(AnchorOnboardingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AnchorOnboardingResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return AnchorOnboardingResponse.from(service.get(id, actorId(jwt), reviewer(jwt)));
    }

    @PostMapping
    public AnchorOnboardingResponse create(
            @RequestBody CreateAnchorOnboardingRequest request, @AuthenticationPrincipal Jwt jwt) {
        if (request == null || jwt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anchor proposal details are required.");
        }
        return AnchorOnboardingResponse.from(service.create(request, actor(jwt), actorId(jwt)));
    }

    @PostMapping("/{id}/steps/{step}/complete")
    public AnchorOnboardingResponse completeStep(
            @PathVariable String id, @PathVariable String step,
            @RequestBody(required = false) CompleteAnchorOnboardingStepRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AnchorOnboardingStep selected;
        try {
            selected = AnchorOnboardingStep.valueOf(step.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown anchor onboarding step.", exception);
        }
        return AnchorOnboardingResponse.from(
                service.completeStep(id, selected, request, actor(jwt), actorId(jwt), reviewer(jwt)));
    }

    private static String actor(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        String name = jwt.getClaimAsString("name");
        return name == null || name.isBlank() ? jwt.getSubject() : name;
    }

    private static String actorId(Jwt jwt) {
        String subjectId = jwt == null ? null : jwt.getClaimAsString("subjectId");
        if (subjectId == null || subjectId.isBlank()) {
            subjectId = jwt == null ? null : jwt.getSubject();
        }
        if (subjectId == null || subjectId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return subjectId;
    }

    private static boolean reviewer(Jwt jwt) {
        String role = jwt == null ? null : jwt.getClaimAsString("role");
        return "BANK_CHECKER".equals(role) || "ADMIN".equals(role);
    }
}
