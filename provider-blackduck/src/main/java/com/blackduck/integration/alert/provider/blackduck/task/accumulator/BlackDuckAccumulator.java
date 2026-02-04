/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.task.accumulator;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.processor.filter.StatefulAlertPage;
import com.blackduck.integration.alert.api.provider.lifecycle.ProviderTask;
import com.blackduck.integration.alert.api.provider.state.ProviderProperties;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.message.model.DateRange;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.NotificationView;
import com.blackduck.integration.exception.IntegrationException;

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

    private final ReentrantLock accumulatingLock = new ReentrantLock();
    private final AtomicBoolean accumulatorRunning = new AtomicBoolean(false);

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
        BlackDuckProperties blackDuckProperties = getProviderProperties();
        if (blackDuckSystemValidator.canConnect(blackDuckProperties, new BlackDuckApiTokenValidator(blackDuckProperties))) {
            accumulateNotifications();
        }
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    private void accumulateNotifications() {
        if (accumulatorRunning.get()) {
            logger.info("Accumulator already running skipping run.");
        } else {
            try {
                accumulatingLock.lock();
                logger.info("Accumulating lock acquired. Preventing other accumulation cycles.");
                accumulatorRunning.set(true);
                Optional<BlackDuckNotificationRetriever> optionalNotificationRetriever = notificationRetrieverFactory.createBlackDuckNotificationRetriever(getProviderProperties());
                if (optionalNotificationRetriever.isPresent()) {
                    DateRange dateRange = searchDateManager.retrieveNextSearchDateRange();
                    logger.info(
                        "Accumulating notifications between {} and {} ",
                        DateUtils.formatDateAsJsonString(dateRange.getStart()),
                        DateUtils.formatDateAsJsonString(dateRange.getEnd())
                    );
                    retrieveAndStoreNotificationsSafely(optionalNotificationRetriever.get(), dateRange);
                }
            } finally {
                accumulatorRunning.set(false);
                accumulatingLock.unlock();
                logger.info("Accumulating lock released. Allowing other accumulation cycles.");
            }
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
        StatefulAlertPage<NotificationUserView, IntegrationException> notificationPage = notificationRetriever.retrievePageOfFilteredNotifications(
            dateRange,
            SUPPORTED_NOTIFICATION_TYPES
        );
        int storedNotifications = 0;
        int batchLimit = getProviderProperties().getNotifcationBatchLimit();
        boolean hasExceedBatchSize = false;
        UUID batchId = UUID.randomUUID();
        try {
            while (!hasExceedBatchSize && !notificationPage.isCurrentPageEmpty()) {
                List<NotificationUserView> currentNotifications = notificationPage.getCurrentModels();
                logger.debug("Retrieved a page of {} notifications for batch: {}", currentNotifications.size(), batchId);

                storedNotifications += storeNotifications(batchId, currentNotifications);
                hasExceedBatchSize = storedNotifications >= batchLimit;
                notificationPage = notificationPage.retrieveNextPage();
            }
            if(hasExceedBatchSize) {
                logger.info("Accumulator batch limit exceeded.  Accumulation cycle stopped.  Sending event to begin processing notifications.");
            }

        } finally {
            if (storedNotifications > 0) {
                eventManager.sendEvent(new NotificationReceivedEvent(getProviderProperties().getConfigId()));
            }
        }
    }

    private int storeNotifications(UUID batchId, List<NotificationUserView> notifications) {
        List<AlertNotificationModel> alertNotifications = convertToAlertNotificationModels(notifications);
        int notificationsWritten = write(batchId, alertNotifications);
        Optional<OffsetDateTime> optionalNextSearchTime = computeLatestNotificationCreatedAtDate(alertNotifications)
            .map(latestNotification -> latestNotification.minusNanos(1000000));
        if (optionalNextSearchTime.isPresent()) {
            OffsetDateTime nextSearchTime = optionalNextSearchTime.get();
            logger.info("Notifications found; the next search time will be: {}", nextSearchTime);
            searchDateManager.saveNextSearchStart(nextSearchTime);
        }

        return notificationsWritten;
    }

    private List<AlertNotificationModel> convertToAlertNotificationModels(List<NotificationUserView> notifications) {
        return notifications
            .stream()
            .sorted(Comparator.comparing(NotificationView::getCreatedAt))
            .map(this::convertToAlertNotificationModel)
            .toList();
    }

    private int write(UUID batchId, List<AlertNotificationModel> contentList) {
        logger.info("Writing {} notifications for provider {} ...", contentList.size(), getProviderProperties().getConfigId());
        List<AlertNotificationModel> savedNotifications = notificationAccessor.saveAllNotificationsInBatch(batchId, contentList);
        logger.info("Saved {} notifications for provider {} ...", savedNotifications.size(), getProviderProperties().getConfigId());
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = savedNotifications.stream()
                .map(AlertNotificationModel::getId)
                .toList();
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug("Saved notifications: {}", joinedIds);
        }
        return savedNotifications.size();
    }

    private AlertNotificationModel convertToAlertNotificationModel(NotificationView notification) {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = DateUtils.fromDateUTC(notification.getCreatedAt());
        String provider = blackDuckProviderKey.getUniversalKey();
        String notificationType = notification.getType().name();
        String jsonContent = notification.getJson();
        String hashOfUrl = createContentId(getProviderProperties().getConfigId(), notification);
        return new AlertNotificationModel(
            null,
            getProviderProperties().getConfigId(),
            provider,
            getProviderProperties().getConfigName(),
            notificationType,
            jsonContent,
            createdAt,
            providerCreationTime,
            false,
            hashOfUrl,
            false
        );
    }

    private String createContentId(Long providerConfigId, NotificationView notification) {
        // generate new default in case the href of notification view is null.
        String contentId = UUID.randomUUID().toString();
        if (null != notification && null != notification.getHref()) {
            try {
                String providerIdAndUrl = String.format("%s-%s", providerConfigId, notification.getHref().string());
                contentId = new DigestUtils("SHA3-256").digestAsHex(providerIdAndUrl);
            } catch (RuntimeException ex) {
                // do nothing use the URL
                logger.debug("Content id hash cannot be generated for notification.", ex);
            }
        }

        return contentId;
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
