package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditCorrelationToNotificationRelationRepository extends JpaRepository<AuditCorrelationToNotificationRelation, AuditCorrelationToNotificationRelationPK> {

    Page<AuditCorrelationToNotificationRelation> findAllByAuditCorrelationId(UUID auditCorrelationId, Pageable pageable);

    void deleteAllByAuditCorrelationId(UUID auditCorrelationId);

}
