package com.ncba.boosta.audit.dto.request;

import java.time.Instant;

public record CreateAuditEventRequest(String action, String entityType, String entityId,
                                      String outcome, String correlationId, String details,
                                      Instant occurredAt) {
}
