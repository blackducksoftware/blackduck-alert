package com.blackducksoftware.integration.alert.common.model;

import java.util.List;

import com.blackducksoftware.integration.alert.database.entity.NotificationContent;

public class NotificationContentList {

    private final List<NotificationContent> notificationContentList;

    public NotificationContentList(final List<NotificationContent> notificationContentList) {
        this.notificationContentList = notificationContentList;
    }

    public List<NotificationContent> getNotificationContentList() {
        return notificationContentList;
    }
}
