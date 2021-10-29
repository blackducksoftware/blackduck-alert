package com.synopsys.integration.alert.performance.serialization;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.wait.WaitJobCondition;

public class NotificationRecievedWaitJobTask implements WaitJobCondition {
    private final NotificationAccessor notificationAccessor;
    private final LocalDateTime startSearchTime;

    public NotificationRecievedWaitJobTask(NotificationAccessor notificationAccessor, LocalDateTime startSearchTime) {
        this.notificationAccessor = notificationAccessor;
        this.startSearchTime = startSearchTime;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        List<AlertNotificationModel> notifications = notificationAccessor.findByCreatedAtBetween(startSearchTime.atOffset(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC));
        return notifications.stream()
            .anyMatch(notification -> notification.getNotificationType().equals(NotificationType.COMPONENT_UNKNOWN_VERSION.name()));
    }
}
