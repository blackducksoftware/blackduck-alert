package com.synopsys.integration.alert.database.audit;

import com.synopsys.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "failed_audit_notifications")
public class AuditFailedNotificationEntity extends BaseEntity {
    private static final long serialVersionUID = 3814109086488324825L;
    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "notification_content")
    private String notificationContent;

    public AuditFailedNotificationEntity() {
        // default constructor for JPA
    }

    public AuditFailedNotificationEntity(Long notificationId, String notificationContent) {
        this.notificationId = notificationId;
        this.notificationContent = notificationContent;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public String getNotificationContent() {
        return notificationContent;
    }
}
