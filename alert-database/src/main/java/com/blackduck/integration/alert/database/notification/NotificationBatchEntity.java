/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import java.util.UUID;

import com.blackduck.integration.alert.database.DatabaseRelation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(NotificationBatchPK.class)
@Table(schema = "alert", name = "raw_notification_batch")
public class NotificationBatchEntity extends DatabaseRelation {
    @Id
    @Column(name = "provider_id")
    private Long providerId;

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    public NotificationBatchEntity() {
    }

    public NotificationBatchEntity(final Long providerId, final UUID batchId, final Long notificationId) {
        this.providerId = providerId;
        this.batchId = batchId;
        this.notificationId = notificationId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public Long getNotificationId() {
        return notificationId;
    }
}
