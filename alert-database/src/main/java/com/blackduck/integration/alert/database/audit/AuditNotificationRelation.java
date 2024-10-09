package com.blackduck.integration.alert.database.audit;

import com.blackduck.integration.alert.database.DatabaseRelation;
import com.blackduck.integration.alert.database.notification.NotificationEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@IdClass(AuditNotificationRelationPK.class)
@Table(schema = "alert", name = "audit_notification_relation")
public class AuditNotificationRelation extends DatabaseRelation {
    @Id
    @Column(name = "audit_entry_id")
    private Long auditEntryId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "id", insertable = false, updatable = false)
    public NotificationEntity notificationContent;
    @ManyToOne
    @JoinColumn(name = "audit_entry_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuditEntryEntity auditEntryEntity;

    public AuditNotificationRelation() {
    }

    public AuditNotificationRelation(Long auditEntryId, Long notificationId) {
        super();
        this.auditEntryId = auditEntryId;
        this.notificationId = notificationId;
    }

    public Long getAuditEntryId() {
        return auditEntryId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public NotificationEntity getNotificationContent() {
        return notificationContent;
    }

    public AuditEntryEntity getAuditEntryEntity() {
        return auditEntryEntity;
    }
}
