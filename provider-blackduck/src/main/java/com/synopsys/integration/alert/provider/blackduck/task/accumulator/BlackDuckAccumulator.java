/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.api.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.api.provider.state.ProviderProperties;
import com.synopsys.integration.alert.api.task.ScheduledTask;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckAccumulator extends ProviderTask {
    private static final List<String> SUPPORTED_NOTIFICATION_TYPES = Stream.of(NotificationType.values())
        .filter(type -> type != NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED)
        .map(Enum::name)
        .collect(Collectors.toList());

    private final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationAccessor notificationAccessor;
    private final BlackDuckSystemValidator blackDuckSystemValidator;
    private final EventManager eventManager;
    private final BlackDuckNotificationRetrieverFactory notificationRetrieverFactory;
    private final BlackDuckAccumulatorSearchDateManager searchDateManager;

    public BlackDuckAccumulator(
        BlackDuckProviderKey blackDuckProviderKey,
        TaskScheduler taskScheduler,
        NotificationAccessor notificationAccessor,
        ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor,
        ProviderProperties providerProperties,
        BlackDuckSystemValidator blackDuckSystemValidator,
        EventManager eventManager,
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory
    ) {
        super(blackDuckProviderKey, taskScheduler, providerProperties);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.notificationAccessor = notificationAccessor;
        this.blackDuckSystemValidator = blackDuckSystemValidator;
        this.eventManager = eventManager;
        this.notificationRetrieverFactory = notificationRetrieverFactory;
        this.searchDateManager = new BlackDuckAccumulatorSearchDateManager(providerTaskPropertiesAccessor, providerProperties.getConfigId(), getTaskName());
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
    }

    @Override
    protected void runProviderTask() {
        if (blackDuckSystemValidator.validate(getProviderProperties())) {
            accumulateNotifications();
        }
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    private void accumulateNotifications() {
        Optional<BlackDuckNotificationRetriever> optionalNotificationRetriever = notificationRetrieverFactory.createBlackDuckNotificationRetriever(getProviderProperties());
        if (optionalNotificationRetriever.isPresent()) {
            DateRange dateRange = searchDateManager.retrieveNextSearchDateRange();
            logger.info("Accumulating notifications between {} and {} ", DateUtils.formatDateAsJsonString(dateRange.getStart()), DateUtils.formatDateAsJsonString(dateRange.getEnd()));
            retrieveAndStoreNotificationsSafely(optionalNotificationRetriever.get(), dateRange);
        }
    }

    private void retrieveAndStoreNotificationsSafely(BlackDuckNotificationRetriever notificationRetriever, DateRange dateRange) {
        try {
            retrieveAndStoreNotifications(notificationRetriever, dateRange);
        } catch (IntegrationException e) {
            logger.error("Error reading notifications", e);
        }
    }

    private void retrieveAndStoreNotifications(BlackDuckNotificationRetriever notificationRetriever, DateRange dateRange) throws IntegrationException {
        StatefulAlertPage<NotificationUserView, IntegrationException> notificationPage = notificationRetriever.retrievePageOfFilteredNotifications(dateRange, SUPPORTED_NOTIFICATION_TYPES);
        while (!notificationPage.isCurrentPageEmpty()) {
            List<NotificationUserView> currentNotifications = notificationPage.getCurrentModels();
            logger.debug("Retrieved a page of {} notifications", currentNotifications.size());
            storeNotifications(currentNotifications);

            notificationPage = notificationPage.retrieveNextPage();
        }
    }

    private void storeNotifications(List<NotificationUserView> notifications) {
        List<AlertNotificationModel> alertNotifications = convertToAlertNotificationModels(notifications);
        write(alertNotifications);
        Optional<OffsetDateTime> optionalNextSearchTime = computeLatestNotificationCreatedAtDate(alertNotifications)
            .map(latestNotification -> latestNotification.plusNanos(1000000));
        if (optionalNextSearchTime.isPresent()) {
            OffsetDateTime nextSearchTime = optionalNextSearchTime.get();
            logger.info("Notifications found; the next search time will be: {}", nextSearchTime);
            searchDateManager.saveNextSearchStart(nextSearchTime);
        }
    }

    private List<AlertNotificationModel> convertToAlertNotificationModels(List<NotificationUserView> notifications) {
        return notifications
            .stream()
            .sorted(Comparator.comparing(NotificationView::getCreatedAt))
            .map(this::convertToAlertNotificationModel)
            .collect(Collectors.toList());
    }

    private void write(List<AlertNotificationModel> contentList) {
        logger.info("Writing {} notifications...", contentList.size());
        List<AlertNotificationModel> savedNotifications = notificationAccessor.saveAllNotifications(contentList);
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = savedNotifications.stream()
                .map(AlertNotificationModel::getId)
                .collect(Collectors.toList());
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug("Saved notifications: {}", joinedIds);
        }
        eventManager.sendEvent(new NotificationReceivedEvent());
    }

    private AlertNotificationModel convertToAlertNotificationModel(NotificationView notification) {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = DateUtils.fromDateUTC(notification.getCreatedAt());
        String provider = blackDuckProviderKey.getUniversalKey();
        String notificationType = notification.getType().name();
        String jsonContent = notification.getJson();
        return new AlertNotificationModel(null, getProviderProperties().getConfigId(), provider, getProviderProperties().getConfigName(), notificationType, jsonContent, createdAt, providerCreationTime, false);
    }

    // Expects that the notifications are sorted oldest to newest
    private Optional<OffsetDateTime> computeLatestNotificationCreatedAtDate(List<AlertNotificationModel> sortedNotifications) {
        if (!sortedNotifications.isEmpty()) {
            int lastIndex = sortedNotifications.size() - 1;
            AlertNotificationModel lastNotification = sortedNotifications.get(lastIndex);
            return Optional.of(lastNotification.getProviderCreationTime());
        }
        return Optional.empty();
    }

}
