/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationCountsPerHourDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = 4204641846025553928L;

    private final String hour;
    private final long notificationCount;

    public NotificationCountsPerHourDiagnosticModel(final String hour, final long notificationCount) {
        this.hour = hour;
        this.notificationCount = notificationCount;
    }

    public String getHour() {
        return hour;
    }

    public long getNotificationCount() {
        return notificationCount;
    }
}
