/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import java.time.OffsetDateTime;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationCountsPerHour extends AlertSerializableModel {
    private static final long serialVersionUID = 3344752544485627290L;

    private final OffsetDateTime accumulationHour;
    private final Long notificationCount;

    public NotificationCountsPerHour(final OffsetDateTime accumulationHour, final Long notificationCount) {
        this.accumulationHour = accumulationHour;
        this.notificationCount = notificationCount;
    }

    public OffsetDateTime getAccumulationHour() {
        return accumulationHour;
    }

    public Long getNotificationCount() {
        return notificationCount;
    }
}
