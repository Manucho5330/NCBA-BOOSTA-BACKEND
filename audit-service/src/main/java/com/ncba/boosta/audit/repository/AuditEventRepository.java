package com.ncba.boosta.audit.repository;

import com.ncba.boosta.audit.models.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findTop500ByOrderByOccurredAtDesc();
    List<AuditEvent> findTop500ByEntityTypeOrderByOccurredAtDesc(String entityType);
}
