/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

public class NotificationTypeCount extends AlertSerializableModel {

    private static final long serialVersionUID = -7022752228360734890L;
    private final NotificationType notificationType;
    private final long count;

    public NotificationTypeCount(NotificationType notificationType, long count) {
        this.notificationType = notificationType;
        this.count = count;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public long getCount() {
        return count;
    }
}
