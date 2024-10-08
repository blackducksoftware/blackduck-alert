package com.synopsys.integration.alert.performance.serialization;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.wait.WaitJobCondition;

public class NotificationReceivedWaitJobTask implements WaitJobCondition {
    private final NotificationAccessor notificationAccessor;
    private final LocalDateTime startSearchTime;
    private final String expectedCompponentName;
    private final @Nullable String expectedComponentVersion;
    private final NotificationType expectedNotificationType;

    public NotificationReceivedWaitJobTask(NotificationAccessor notificationAccessor, LocalDateTime startSearchTime, String expectedCompponentName,
        @Nullable String expectedComponentVersion, NotificationType expectedNotificationType) {
        this.notificationAccessor = notificationAccessor;
        this.startSearchTime = startSearchTime;
        this.expectedCompponentName = expectedCompponentName;
        this.expectedComponentVersion = expectedComponentVersion;
        this.expectedNotificationType = expectedNotificationType;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        return getNotificationContent().isPresent();
    }

    public Optional<String> getNotificationContent() {
        Predicate<String> componentFilter = (content) -> content.contains(expectedCompponentName);

        if (StringUtils.isNotBlank(expectedComponentVersion)) {
            componentFilter = componentFilter.and((content) -> content.contains(expectedComponentVersion));
        }
        AlertPagedModel<AlertNotificationModel> page = notificationAccessor.findByCreatedAtBetween(startSearchTime.atOffset(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC), AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE);

        return page.getModels().stream()
            .filter(notification -> notification.getNotificationType().equals(expectedNotificationType.name()))
            .map(AlertNotificationModel::getContent)
            .filter(componentFilter)
            .findFirst();
    }
}
