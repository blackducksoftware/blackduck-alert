/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationContentData;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

// TODO create an interface for this to implement avoid "raw use of parameterized class"
//  List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel);
public abstract class NotificationDetailExtractor<T extends NotificationContentComponent, U extends NotificationContentData<T>> {
    private final NotificationType notificationType;
    private final Class<U> notificationViewClass;
    private final Gson gson;

    public NotificationDetailExtractor(NotificationType notificationType, Class<U> notificationViewClass, Gson gson) {
        this.notificationType = notificationType;
        this.notificationViewClass = notificationViewClass;
        this.gson = gson;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public final List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel) {
        U notificationView = gson.fromJson(alertNotificationModel.getContent(), notificationViewClass);
        T notificationContent = notificationView.getContent();
        return extractDetailedContent(alertNotificationModel, notificationContent);
    }

    protected abstract List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, T notificationContent);

}
