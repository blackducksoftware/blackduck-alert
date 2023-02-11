package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationTypeCount extends AlertSerializableModel {

    private static final long serialVersionUID = -7022752228360734890L;
    private final String notificationType;
    private final long count;

    public NotificationTypeCount(String notificationType, long count) {
        this.notificationType = notificationType;
        this.count = count;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public long getCount() {
        return count;
    }
}
