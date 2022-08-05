package com.synopsys.integration.alert.database.distribution.workflow;

import java.io.Serializable;
import java.util.UUID;

public class AuditCorrelationToEntityRelationPK implements Serializable {
    private static final long serialVersionUID = 5371783708376637560L;
    private UUID auditCorrelationId;
    private Long auditEntryId;

    public AuditCorrelationToEntityRelationPK() {
    }

    public AuditCorrelationToEntityRelationPK(UUID auditCorrelationId, Long auditEntryId) {
        this.auditCorrelationId = auditCorrelationId;
        this.auditEntryId = auditEntryId;
    }

    public UUID getAuditCorrelationId() {
        return auditCorrelationId;
    }

    public void setAuditCorrelationId(UUID auditCorrelationId) {
        this.auditCorrelationId = auditCorrelationId;
    }

    public Long getAuditEntryId() {
        return auditEntryId;
    }

    public void setAuditEntryId(Long auditEntryId) {
        this.auditEntryId = auditEntryId;
    }
}
