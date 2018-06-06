package com.blackducksoftware.integration.hub.alert.event;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModels;

public class NotificationListEvent extends AlertEvent {
    public NotificationListEvent(final String destination, final NotificationModels notificationModels) {
        super(destination, notificationModels);
    }
}
