package com.synopsys.integration.alert.database.audit;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

@Entity
@IdClass(AuditFailedNotificationRelationPK.class)
@Table(schema = "alert", name = "failed_audit_notification_relation")
public class AuditFailedNotificationRelation extends DatabaseRelation {
    @Id
    @Column(name = "failed_audit_entry_id")
    private UUID failedAuditEntryId;
    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "id", insertable = false, updatable = false)
    private NotificationEntity notificationContent;
    @ManyToOne
    @JoinColumn(name = "failed_audit_entry_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuditFailedEntity auditFailedEntity;

    public AuditFailedNotificationRelation() {
    }

    public AuditFailedNotificationRelation(UUID failedAuditEntryId, Long notificationId) {
        this.failedAuditEntryId = failedAuditEntryId;
        this.notificationId = notificationId;
    }

    public UUID getFailedAuditEntryId() {
        return failedAuditEntryId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public NotificationEntity getNotificationContent() {
        return notificationContent;
    }

    public AuditFailedEntity getAuditFailedEntity() {
        return auditFailedEntity;
    }
}
