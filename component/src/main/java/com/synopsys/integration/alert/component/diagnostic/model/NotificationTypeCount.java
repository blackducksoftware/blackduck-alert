package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
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
