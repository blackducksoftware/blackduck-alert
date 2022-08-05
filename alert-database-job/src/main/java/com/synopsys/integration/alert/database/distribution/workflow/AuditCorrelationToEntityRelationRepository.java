package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditCorrelationToEntityRelationRepository extends JpaRepository<AuditCorrelationToEntityRelation, AuditCorrelationToEntityRelationPK> {

    Page<AuditCorrelationToEntityRelation> findAllByAuditCorrelationId(UUID auditCorrelationId, Pageable pageable);

    void deleteAllByAuditCorrelationId(UUID auditCorrelationId);

}
