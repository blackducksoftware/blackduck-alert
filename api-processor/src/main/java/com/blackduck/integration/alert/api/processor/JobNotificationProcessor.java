/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.distribute.ProcessedNotificationDetails;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;

@Component
public class JobNotificationProcessor {
    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final NotificationContentProcessor notificationContentProcessor;
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;

    @Autowired
    public JobNotificationProcessor(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        NotificationContentProcessor notificationContentProcessor,
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.notificationContentProcessor = notificationContentProcessor;
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
    }

    public void processNotificationForJob(ProcessedNotificationDetails processedNotificationDetails, ProcessingType processingType, List<AlertNotificationModel> notifications) {
        try {
            processAndDistribute(processedNotificationDetails, processingType, notifications);
        } finally {
            clearCaches();
        }
    }

    private void processAndDistribute(ProcessedNotificationDetails processedNotificationDetails, ProcessingType processingType, List<AlertNotificationModel> notifications) {
        List<NotificationContentWrapper> notificationContentWrappers = notifications
            .stream()
            .map(notificationDetailExtractionDelegator::wrapNotification)
            .flatMap(List::stream)
            .map(DetailedNotificationContent::getNotificationContentWrapper)
            .collect(Collectors.toList());

        ProcessedProviderMessageHolder processedMessageHolder = notificationContentProcessor.processNotificationContent(processingType, notificationContentWrappers);

        providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);
    }

    private void clearCaches() {
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }

}
