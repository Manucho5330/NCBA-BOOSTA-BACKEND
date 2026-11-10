package com.ncba.boosta.audit.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String actor;
    @Column(nullable = false, length = 80)
    private String action;
    @Column(nullable = false, length = 80)
    private String entityType;
    @Column(length = 160)
    private String entityId;
    @Column(nullable = false, length = 30)
    private String outcome;
    @Column(length = 100)
    private String correlationId;
    @Column(length = 4000)
    private String details;
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEvent() { }

    public AuditEvent(String actor, String action, String entityType, String entityId,
                      String outcome, String correlationId, String details, Instant occurredAt) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.details = details;
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getOutcome() { return outcome; }
    public String getCorrelationId() { return correlationId; }
    public String getDetails() { return details; }
    public Instant getOccurredAt() { return occurredAt; }
}
