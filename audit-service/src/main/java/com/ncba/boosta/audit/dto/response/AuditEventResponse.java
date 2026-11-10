package com.ncba.boosta.audit.dto.response;

import com.ncba.boosta.audit.models.AuditEvent;
import java.time.Instant;

public record AuditEventResponse(String actor, String action, String entityType,
                                 String entityId, String outcome, String correlationId,
                                 String details, Instant occurredAt) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getActor(), event.getAction(),
                event.getEntityType(), event.getEntityId(), event.getOutcome(),
                event.getCorrelationId(), event.getDetails(), event.getOccurredAt());
    }
}
