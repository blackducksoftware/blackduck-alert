/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;

@Component
public final class NotificationProcessorV2 {
    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final JobNotificationMapper jobNotificationMapper;
    private final NotificationContentProcessor notificationContentProcessor;
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;
    private final NotificationAccessor notificationAccessor;

    @Autowired
    public NotificationProcessorV2(
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

    public final void processNotifications(List<AlertNotificationModel> notifications, Collection<FrequencyType> frequencies) {
        try {
            processAndDistribute(notifications, frequencies);
            notificationAccessor.setNotificationsProcessed(notifications);
        } finally {
            clearCaches();
        }
    }

    private void processAndDistribute(List<AlertNotificationModel> notifications, Collection<FrequencyType> frequencies) {
        List<DetailedNotificationContent> filterableNotifications = notifications
                                                                        .stream()
                                                                        .map(notificationDetailExtractionDelegator::wrapNotification)
                                                                        .flatMap(List::stream)
                                                                        .collect(Collectors.toList());
        StatefulAlertPage<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(filterableNotifications, frequencies);
        do {
            for (FilteredJobNotificationWrapper jobNotificationWrapper : mappedNotifications.getCurrentPage().getModels()) {
                processAndDistribute(jobNotificationWrapper);
            }
            mappedNotifications = mappedNotifications.retrieveNextPage();
        } while (mappedNotifications.hasNextPage() || !mappedNotifications.isEmpty());
    }

    private void processAndDistribute(FilteredJobNotificationWrapper jobNotificationWrapper) {
        List<NotificationContentWrapper> filteredNotifications = jobNotificationWrapper.getJobNotifications();
        Set<Long> notificationIds = filteredNotifications
                                        .stream()
                                        .map(NotificationContentWrapper::getNotificationId)
                                        .collect(Collectors.toSet());

        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobNotificationWrapper.getJobId(), jobNotificationWrapper.getChannelName(), notificationIds);
        ProviderMessageHolder providerMessageHolder = notificationContentProcessor.processNotificationContent(jobNotificationWrapper.getProcessingType(), filteredNotifications);

        providerMessageDistributor.distribute(processedNotificationDetails, providerMessageHolder);
    }

    private void clearCaches() {
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }

}
