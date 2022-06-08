package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = -6054332942386217935L;

    private final Long numberOfNotifications;
    private final Long numberOfNotificationsProcessed;
    private final Long numberOfNotificationsUnprocessed;

    public NotificationDiagnosticModel(Long numberOfNotifications, Long numberOfNotificationsProcessed, Long numberOfNotificationsUnprocessed) {
        this.numberOfNotifications = numberOfNotifications;
        this.numberOfNotificationsProcessed = numberOfNotificationsProcessed;
        this.numberOfNotificationsUnprocessed = numberOfNotificationsUnprocessed;
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
}
