package com.synopsys.integration.alert.database.distribution.workflow;

import java.io.Serializable;
import java.util.UUID;

public class AuditCorrelationToNotificationRelationPK implements Serializable {
    private static final long serialVersionUID = 5371783708376637560L;
    private UUID auditCorrelationId;
    private Long notificationId;

    public AuditCorrelationToNotificationRelationPK() {
    }

    public AuditCorrelationToNotificationRelationPK(UUID auditCorrelationId, Long notificationId) {
        this.auditCorrelationId = auditCorrelationId;
        this.notificationId = notificationId;
    }

    public UUID getAuditCorrelationId() {
        return auditCorrelationId;
    }

    public void setAuditCorrelationId(UUID auditCorrelationId) {
        this.auditCorrelationId = auditCorrelationId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long auditEntryId) {
        this.notificationId = auditEntryId;
    }
}
