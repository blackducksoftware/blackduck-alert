/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.detail;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.blackduck.api.manual.view.NotificationView;

// TODO create an interface for this to implement avoid "raw use of parameterized class"
//  List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel);
public abstract class NotificationDetailExtractor<U extends NotificationView> {
    private final Class<U> notificationViewClass;

    protected NotificationDetailExtractor(Class<U> notificationViewClass) {
        this.notificationViewClass = notificationViewClass;
    }

    public Class<U> getNotificationViewClass() {
        return notificationViewClass;
    }

    public abstract List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, U notificationView);

}
