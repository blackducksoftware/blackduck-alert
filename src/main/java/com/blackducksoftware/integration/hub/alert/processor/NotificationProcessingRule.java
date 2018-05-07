package com.blackducksoftware.integration.hub.alert.processor;

import java.util.Map;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;

public abstract class NotificationProcessingRule {
    private final NotificationType notificationType;

    public NotificationProcessingRule(final NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isApplicable(final CommonNotificationState commonNotificationState) {
        return notificationType == commonNotificationState.getType();
    }

    public abstract void apply(Map<String, NotificationModel> modelMap, CommonNotificationState commonNotificationState);

}
