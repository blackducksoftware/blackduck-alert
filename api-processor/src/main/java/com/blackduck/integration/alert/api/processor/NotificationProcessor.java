/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.distribute.ProcessedNotificationDetails;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.filter.FilteredJobNotificationWrapper;
import com.blackduck.integration.alert.api.processor.filter.JobNotificationMapper;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;

@Component
// TODO rename to WorkflowNotificationProcessor
public final class NotificationProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final JobNotificationMapper jobNotificationMapper;
    private final NotificationContentProcessor notificationContentProcessor;
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;
    private final NotificationAccessor notificationAccessor;

    @Autowired
    public NotificationProcessor(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        JobNotificationMapper jobNotificationMapper,
        NotificationContentProcessor notificationContentProcessor,
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches,
        NotificationAccessor notificationAccessor
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.jobNotificationMapper = jobNotificationMapper;
        this.notificationContentProcessor = notificationContentProcessor;
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
        this.notificationAccessor = notificationAccessor;
    }

    public void processNotifications(List<AlertNotificationModel> notifications, List<FrequencyType> frequencies) {
        try {
            logNotifications("Start processing notifications: {}", notifications);
            processAndDistribute(notifications, frequencies);
            notificationAccessor.setNotificationsProcessed(notifications);
            logNotifications("Finished processing notifications: {}", notifications);
        } finally {
            clearCaches();
        }
    }

    private void logNotifications(String messageFormat, List<AlertNotificationModel> notifications) {
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = notifications.stream()
                .map(AlertNotificationModel::getId)
                .collect(Collectors.toList());
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug(messageFormat, joinedIds);
        }
    }

    private void processAndDistribute(List<AlertNotificationModel> notifications, List<FrequencyType> frequencies) {
        List<DetailedNotificationContent> filterableNotifications = notifications
            .stream()
            .map(notificationDetailExtractionDelegator::wrapNotification)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        jobNotificationMapper.mapJobsToNotifications(filterableNotifications, frequencies).stream()
            .forEach(filteredJobNotificationWrapper -> processAndDistribute(filteredJobNotificationWrapper));
    }

    private void processAndDistribute(FilteredJobNotificationWrapper jobNotificationWrapper) {
        List<NotificationContentWrapper> filteredNotifications = jobNotificationWrapper.getJobNotifications();
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(
            UUID.randomUUID(),
            jobNotificationWrapper.getJobId(),
            jobNotificationWrapper.getChannelName(),
            jobNotificationWrapper.getJobName()
        );
        ProcessedProviderMessageHolder processedMessageHolder = notificationContentProcessor.processNotificationContent(
            jobNotificationWrapper.getProcessingType(),
            filteredNotifications
        );

        providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);
    }

    private void clearCaches() {
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }

}
