/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.blackduck.integration.util.Stringable;

// NotificationContentComponent is not serializable, so this class cannot be serializable (and doesn't need to be)
public class NotificationContentWrapper extends Stringable {
    private final AlertNotificationModel alertNotificationModel;
    private final NotificationContentComponent notificationContent;

    private final Class<? extends NotificationContentComponent> notificationContentClass;

    public NotificationContentWrapper(
        AlertNotificationModel alertNotificationModel,
        NotificationContentComponent notificationContent,
        Class<? extends NotificationContentComponent> notificationContentClass
    ) {
        this.alertNotificationModel = alertNotificationModel;
        this.notificationContent = notificationContent;
        this.notificationContentClass = notificationContentClass;
    }

    public AlertNotificationModel getAlertNotificationModel() {
        return alertNotificationModel;
    }

    public NotificationContentComponent getNotificationContent() {
        return notificationContent;
    }

    public String extractNotificationType() {
        return getAlertNotificationModel().getNotificationType();
    }

    public Long getNotificationId() {
        return getAlertNotificationModel().getId();
    }

    public Class<? extends NotificationContentComponent> getNotificationContentClass() {
        return notificationContentClass;
    }

}
