/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckAccumulatorDateRangeCreator;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckNotificationRetriever;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckValidator;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckAccumulator extends ProviderTask {
    public static final String TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE = "last.search.end.date";
    private static final List<NotificationType> SUPPORTED_NOTIFICATION_TYPES = Stream.of(NotificationType.values())
                                                                                   .filter(type -> type != NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED)
                                                                                   .collect(Collectors.toList());

    private final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationAccessor notificationAccessor;
    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private final BlackDuckValidator blackDuckValidator;
    private final EventManager eventManager;
    private final BlackDuckAccumulatorDateRangeCreator dateRangeCreator;

    public BlackDuckAccumulator(
        BlackDuckProviderKey blackDuckProviderKey,
        TaskScheduler taskScheduler,
        NotificationAccessor notificationAccessor,
        ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor,
        ProviderProperties providerProperties,
        BlackDuckValidator blackDuckValidator,
        EventManager eventManager,
        BlackDuckAccumulatorDateRangeCreator dateRangeCreator
    ) {
        super(blackDuckProviderKey, taskScheduler, providerProperties);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.notificationAccessor = notificationAccessor;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
        this.blackDuckValidator = blackDuckValidator;
        this.eventManager = eventManager;
        this.dateRangeCreator = dateRangeCreator;
    }

    @Override
    public void runProviderTask() {
        if (blackDuckValidator.validate(getProviderProperties())) {
            accumulate();
        }
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    public void accumulate() {
        DateRange dateRange = dateRangeCreator.createDateRange(getTaskName());
        OffsetDateTime nextSearchStartTime = accumulate(dateRange);
        String nextSearchStartString = formatDate(nextSearchStartTime);
        logger.info("Accumulator Next Range Start Time: {} ", nextSearchStartString);
        saveNextSearchStart(nextSearchStartString);
    }

    private String formatDate(OffsetDateTime date) {
        return DateUtils.formatDate(date, RestConstants.JSON_DATE_FORMAT);
    }

    protected void saveNextSearchStart(String nextSearchStart) {
        providerTaskPropertiesAccessor.setTaskProperty(getProviderProperties().getConfigId(), getTaskName(), BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE, nextSearchStart);
    }

    protected OffsetDateTime accumulate(DateRange dateRange) {
        OffsetDateTime currentStartTime = dateRange.getStart();
        Optional<OffsetDateTime> latestNotificationCreatedAtDate = Optional.empty();

        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(getProviderProperties());

        List<NotificationView> notifications = notificationRetriever.retrieveFilteredNotifications(dateRange, SUPPORTED_NOTIFICATION_TYPES);
        if (!notifications.isEmpty()) {
            List<NotificationView> sortedNotifications = sort(notifications);
            List<AlertNotificationModel> contentList = process(sortedNotifications);
            write(contentList);
            latestNotificationCreatedAtDate = getLatestNotificationCreatedAtDate(sortedNotifications);
        }
        return calculateNextStartTime(latestNotificationCreatedAtDate, currentStartTime);
    }

    protected List<AlertNotificationModel> process(List<NotificationView> notifications) {
        logger.info("Processing accumulated notifications");
        return notifications
                   .stream()
                   .map(this::createContent)
                   .collect(Collectors.toList());
    }

    protected void write(List<AlertNotificationModel> contentList) {
        logger.info("Writing {} Notifications...", contentList.size());
        notificationAccessor.saveAllNotifications(contentList);
        eventManager.sendEvent(new NotificationReceivedEvent());
    }

    private List<NotificationView> sort(List<NotificationView> notifications) {
        return notifications
                   .stream()
                   .sorted(Comparator.comparing(NotificationView::getCreatedAt))
                   .collect(Collectors.toList());
    }

    private AlertNotificationModel createContent(NotificationView notification) {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = DateUtils.fromDateUTC(notification.getCreatedAt());
        String provider = blackDuckProviderKey.getUniversalKey();
        String notificationType = notification.getType().name();
        String jsonContent = notification.getJson();
        return new AlertNotificationModel(null, getProviderProperties().getConfigId(), provider, null, notificationType, jsonContent, createdAt, providerCreationTime, false);
    }

    // Expects that the notifications are sorted oldest to newest
    private Optional<OffsetDateTime> getLatestNotificationCreatedAtDate(List<NotificationView> sortedNotificationList) {
        if (!sortedNotificationList.isEmpty()) {
            int lastIndex = sortedNotificationList.size() - 1;
            NotificationView notificationView = sortedNotificationList.get(lastIndex);
            OffsetDateTime createdAtDate = DateUtils.fromDateUTC(notificationView.getCreatedAt());
            return Optional.of(createdAtDate);
        }
        return Optional.empty();
    }

    private OffsetDateTime calculateNextStartTime(Optional<OffsetDateTime> latestNotificationCreatedAt, OffsetDateTime currentStartDate) {
        OffsetDateTime newStartDate = currentStartDate;
        if (latestNotificationCreatedAt.isPresent()) {
            OffsetDateTime latestNotification = latestNotificationCreatedAt.get();
            // increment 1 millisecond
            newStartDate = latestNotification.plusNanos(1000000);
            logger.info("Notifications found; updating to latest notification found");
        } else {
            logger.info("No notifications found; using current search time");
        }
        return newStartDate;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
    }

}
