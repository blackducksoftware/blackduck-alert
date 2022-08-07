package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(AuditCorrelationToNotificationRelationPK.class)
@Table(schema = "alert", name = "audit_correlation_to_notification_relation")
public class AuditCorrelationToNotificationRelation extends DatabaseRelation {

    @Id
    @Column(name = "audit_correlation_id")
    private UUID auditCorrelationId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    public AuditCorrelationToNotificationRelation() {
    }

    public AuditCorrelationToNotificationRelation(UUID auditCorrelationId, Long notificationId) {
        this.auditCorrelationId = auditCorrelationId;
        this.notificationId = notificationId;
    }

    public UUID getAuditCorrelationId() {
        return auditCorrelationId;
    }

    public Long getNotificationId() {
        return notificationId;
    }
}
