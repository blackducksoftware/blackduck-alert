package com.blackducksoftware.integration.hub.alert.event;

import java.util.List;

import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class DBStoreEvent extends AbstractEvent {
    private final List<NotificationEvent> notificationList;

    public DBStoreEvent(final List<NotificationEvent> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public String getTopic() {
        return "DB_STORE_EVENT";
    }

    public List<NotificationEvent> getNotificationList() {
        return notificationList;
    }
}
