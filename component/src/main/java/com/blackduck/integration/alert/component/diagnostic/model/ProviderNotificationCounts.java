/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ProviderNotificationCounts extends AlertSerializableModel {

    private static final long serialVersionUID = -8629438089576433489L;
    private final long providerConfigId;

    private final List<NotificationTypeCount> notificationCounts;

    private final List<NotificationCountsPerHourDiagnosticModel> notificationsPerHour;

    public ProviderNotificationCounts(long providerConfigId, List<NotificationTypeCount> notificationCounts, List<NotificationCountsPerHourDiagnosticModel> notificationsPerHour) {
        this.providerConfigId = providerConfigId;
        this.notificationCounts = notificationCounts;
        this.notificationsPerHour = notificationsPerHour;
    }

    public long getProviderConfigId() {
        return providerConfigId;
    }

    public List<NotificationTypeCount> getNotificationCounts() {
        return notificationCounts;
    }

    public List<NotificationCountsPerHourDiagnosticModel> getNotificationsPerHour() {
        return notificationsPerHour;
    }
}
