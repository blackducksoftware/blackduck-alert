package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(AuditCorrelationToEntityRelationPK.class)
@Table(schema = "alert", name = "audit_correlation_to_entry_relation")
public class AuditCorrelationToEntityRelation extends DatabaseRelation {

    @Id
    @Column(name = "audit_correlation_id")
    private UUID auditCorrelationId;

    @Id
    @Column(name = "audit_entry_id")
    private Long auditEntryId;

    public AuditCorrelationToEntityRelation() {
    }

    public AuditCorrelationToEntityRelation(UUID auditCorrelationId, Long auditEntryId) {
        this.auditCorrelationId = auditCorrelationId;
        this.auditEntryId = auditEntryId;
    }

    public UUID getAuditCorrelationId() {
        return auditCorrelationId;
    }

    public Long getAuditEntryId() {
        return auditEntryId;
    }
}
