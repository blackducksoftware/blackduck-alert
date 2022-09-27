package com.synopsys.integration.alert.database.distribution.workflow;

import java.io.Serializable;
import java.util.UUID;

public class NotificationCorrelationToNotificationRelationPK implements Serializable {
    private static final long serialVersionUID = 5371783708376637560L;
    private UUID notificationCorrelationId;
    private Long notificationId;

    public NotificationCorrelationToNotificationRelationPK() {
    }

    public NotificationCorrelationToNotificationRelationPK(UUID notificationCorrelationId, Long notificationId) {
        this.notificationCorrelationId = notificationCorrelationId;
        this.notificationId = notificationId;
    }

    public UUID getNotificationCorrelationId() {
        return notificationCorrelationId;
    }

    public void setNotificationCorrelationId(UUID notificationCorrelationId) {
        this.notificationCorrelationId = notificationCorrelationId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}
