package com.ncba.boosta.audit.services;

import com.ncba.boosta.audit.models.AuditEvent;
import com.ncba.boosta.audit.repository.AuditEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class AuditService {
    private final AuditEventRepository events;

    public AuditService(AuditEventRepository events) {
        this.events = events;
    }

    public AuditEvent record(AuditRequest request) {
        require(request.actor(), "actor");
        require(request.action(), "action");
        require(request.entityType(), "entityType");
        require(request.outcome(), "outcome");
        if (request.details() != null && request.details().length() > 4000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audit details exceed 4000 characters.");
        }
        return events.save(new AuditEvent(request.actor().trim(), request.action().trim(),
                request.entityType().trim(), request.entityId(), request.outcome().trim(),
                request.correlationId(), request.details(), request.occurredAt()));
    }

    public List<AuditEvent> list(String entityType) {
        return entityType == null || entityType.isBlank()
                ? events.findTop500ByOrderByOccurredAtDesc()
                : events.findTop500ByEntityTypeOrderByOccurredAtDesc(entityType.trim());
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required.");
        }
    }

    public record AuditRequest(String actor, String action, String entityType, String entityId,
                               String outcome, String correlationId, String details, Instant occurredAt) { }
}
