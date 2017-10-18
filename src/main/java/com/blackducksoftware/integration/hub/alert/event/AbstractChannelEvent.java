package com.blackducksoftware.integration.hub.alert.event;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;

public abstract class AbstractChannelEvent extends AbstractEvent {
    private final NotificationEntity notificationEntity;

    public AbstractChannelEvent(final NotificationEntity notificationEntity) {
        super();
        this.notificationEntity = notificationEntity;
    }

    public NotificationEntity getNotificationEntity() {
        return notificationEntity;
    }
}
