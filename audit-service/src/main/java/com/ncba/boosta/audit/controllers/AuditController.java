package com.ncba.boosta.audit.controllers;

import com.ncba.boosta.audit.services.AuditService;
import com.ncba.boosta.audit.dto.request.CreateAuditEventRequest;
import com.ncba.boosta.audit.dto.response.AuditEventResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/audit/events")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public AuditEventResponse record(
            @RequestBody CreateAuditEventRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audit event is required.");
        }
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "An authenticated actor is required.");
        }
        AuditService.AuditRequest attributed = new AuditService.AuditRequest(jwt.getSubject(),
                request.action(), request.entityType(), request.entityId(), request.outcome(),
                request.correlationId(), request.details(), request.occurredAt());
        return AuditEventResponse.from(auditService.record(attributed));
    }

    @GetMapping
    public List<AuditEventResponse> list(@RequestParam(required = false) String entityType) {
        return auditService.list(entityType).stream().map(AuditEventResponse::from).toList();
    }
}
