package com.blackducksoftware.integration.hub.alert.processor;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.util.Stringable;

public class NotificationProcessingModel extends Stringable {
    private final NotificationContentDetail contentDetail;
    private final CommonNotificationState commonNotificationState;
    private final NotificationContent content;
    private final NotificationCategoryEnum notificationType;

    public NotificationProcessingModel(final NotificationContentDetail contentDetail, final CommonNotificationState commonNotificationState, final NotificationContent content, final NotificationCategoryEnum notificationType) {
        this.contentDetail = contentDetail;
        this.commonNotificationState = commonNotificationState;
        this.content = content;
        this.notificationType = notificationType;
    }

    public NotificationContentDetail getContentDetail() {
        return contentDetail;
    }

    public CommonNotificationState getCommonNotificationState() {
        return commonNotificationState;
    }

    public NotificationContent getContent() {
        return content;
    }

    public NotificationCategoryEnum getNotificationType() {
        return notificationType;
    }
}
