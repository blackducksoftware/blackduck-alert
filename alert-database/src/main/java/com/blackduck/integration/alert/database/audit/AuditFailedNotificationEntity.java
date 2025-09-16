/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.audit;

import com.blackduck.integration.alert.database.BasePersistableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "failed_audit_notifications")
public class AuditFailedNotificationEntity extends BasePersistableEntity<Long> {
    private static final long serialVersionUID = 3814109086488324825L;

    @Column(name = "notification_content")
    private String notificationContent;

    public AuditFailedNotificationEntity() {
        // default constructor for JPA
    }

    public AuditFailedNotificationEntity(Long notificationId, String notificationContent, boolean isNew) {
        this.setId(notificationId);
        this.notificationContent = notificationContent;
        this.setIsNew(isNew);
    }

    public Long getNotificationId() {
        return getId();
    }

    public String getNotificationContent() {
        return notificationContent;
    }
}
