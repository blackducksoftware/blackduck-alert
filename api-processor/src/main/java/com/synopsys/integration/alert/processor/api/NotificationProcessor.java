/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;

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
        StatefulAlertPage<FilteredJobNotificationWrapper, RuntimeException> statefulAlertPage = jobNotificationMapper.mapJobsToNotifications(filterableNotifications, frequencies);
        // If there are elements to process in the current page or if there are more pages, keep looping.
        while (!statefulAlertPage.isCurrentPageEmpty() || statefulAlertPage.hasNextPage()) {
            for (FilteredJobNotificationWrapper jobNotificationWrapper : statefulAlertPage.getCurrentModels()) {
                processAndDistribute(jobNotificationWrapper);
            }
            statefulAlertPage = statefulAlertPage.retrieveNextPage();
        }
    }

    private void processAndDistribute(FilteredJobNotificationWrapper jobNotificationWrapper) {
        List<NotificationContentWrapper> filteredNotifications = jobNotificationWrapper.getJobNotifications();
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobNotificationWrapper.getJobId(), jobNotificationWrapper.getChannelName(), jobNotificationWrapper.getJobName());
        ProcessedProviderMessageHolder processedMessageHolder = notificationContentProcessor.processNotificationContent(jobNotificationWrapper.getProcessingType(), filteredNotifications);

        providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);
    }

    private void clearCaches() {
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }

}
