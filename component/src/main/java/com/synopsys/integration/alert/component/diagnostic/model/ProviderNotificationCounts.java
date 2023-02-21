package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ProviderNotificationCounts extends AlertSerializableModel {

    private static final long serialVersionUID = -8629438089576433489L;
    private final long providerConfigId;

    private final List<NotificationTypeCount> notificationCounts;

    public ProviderNotificationCounts(long providerConfigId, List<NotificationTypeCount> notificationCounts) {
        this.providerConfigId = providerConfigId;
        this.notificationCounts = notificationCounts;
    }

    public long getProviderConfigId() {
        return providerConfigId;
    }

    public List<NotificationTypeCount> getNotificationCounts() {
        return notificationCounts;
    }
}
