/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = -6054332942386217935L;

    private final Long numberOfNotifications;
    private final Long numberOfNotificationsProcessed;
    private final Long numberOfNotificationsUnprocessed;

    private final List<ProviderNotificationCounts> providerNotificationCounts;

    public NotificationDiagnosticModel(
        Long numberOfNotifications,
        Long numberOfNotificationsProcessed,
        Long numberOfNotificationsUnprocessed,
        List<ProviderNotificationCounts> providerNotificationCounts
    ) {
        this.numberOfNotifications = numberOfNotifications;
        this.numberOfNotificationsProcessed = numberOfNotificationsProcessed;
        this.numberOfNotificationsUnprocessed = numberOfNotificationsUnprocessed;
        this.providerNotificationCounts = providerNotificationCounts;
    }

    public Long getNumberOfNotifications() {
        return numberOfNotifications;
    }

    public Long getNumberOfNotificationsProcessed() {
        return numberOfNotificationsProcessed;
    }

    public Long getNumberOfNotificationsUnprocessed() {
        return numberOfNotificationsUnprocessed;
    }

    public List<ProviderNotificationCounts> getProviderNotificationCounts() {
        return providerNotificationCounts;
    }
}
