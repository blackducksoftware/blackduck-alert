/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;

// TODO create an interface for this to implement avoid "raw use of parameterized class"
//  List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel);
public abstract class NotificationDetailExtractor<U extends NotificationView> {
    private final Class<U> notificationViewClass;

    public NotificationDetailExtractor(Class<U> notificationViewClass) {
        this.notificationViewClass = notificationViewClass;
    }

    public Class<U> getNotificationViewClass() {
        return notificationViewClass;
    }

    public abstract List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, U notificationView);

}
