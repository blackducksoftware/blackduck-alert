package com.blackduck.integration.alert.database.notification;

import java.io.Serializable;
import java.util.UUID;

public class NotificationBatchPK implements Serializable {
    private static final long serialVersionUID = -2386387064939665605L;

    private Long providerId;
    private Long notificationId;
    private UUID batchId;

    public NotificationBatchPK() {
        // JPA requires empty default constructor
    }

    public NotificationBatchPK(Long providerId, Long notificationId, UUID batchId) {
        this.providerId = providerId;
        this.notificationId = notificationId;
        this.batchId = batchId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(final Long providerId) {
        this.providerId = providerId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(final Long notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(final UUID batchId) {
        this.batchId = batchId;
    }
}
