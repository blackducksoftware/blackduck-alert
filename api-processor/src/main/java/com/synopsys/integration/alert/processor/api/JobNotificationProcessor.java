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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;

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
