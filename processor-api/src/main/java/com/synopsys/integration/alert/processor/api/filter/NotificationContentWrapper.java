/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;

public class NotificationContentWrapper extends AlertSerializableModel {
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
