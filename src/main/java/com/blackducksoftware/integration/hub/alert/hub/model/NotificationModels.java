package com.blackducksoftware.integration.hub.alert.hub.model;

import java.util.List;

public class NotificationModels {

    private final List<NotificationModel> notificationModelList;

    public NotificationModels(final List<NotificationModel> notificationModelList) {
        this.notificationModelList = notificationModelList;
    }

    public List<NotificationModel> getNotificationModelList() {
        return notificationModelList;
    }

}
