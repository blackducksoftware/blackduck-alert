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

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;

@Component
public final class NotificationProcessorV2 {
    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final JobNotificationMapper jobNotificationMapper;
    private final ProviderMessageExtractionDelegator providerMessageExtractionDelegator;
    private final ProjectMessageDigester projectMessageDigester;
    private final ProjectMessageSummarizer projectMessageSummarizer;
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;
    private final NotificationAccessor notificationAccessor;

    @Autowired
    public NotificationProcessorV2(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        JobNotificationMapper jobNotificationMapper,
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator,
        ProjectMessageDigester projectMessageDigester,
        ProjectMessageSummarizer projectMessageSummarizer,
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches,
        NotificationAccessor notificationAccessor
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.jobNotificationMapper = jobNotificationMapper;
        this.providerMessageExtractionDelegator = providerMessageExtractionDelegator;
        this.projectMessageDigester = projectMessageDigester;
        this.projectMessageSummarizer = projectMessageSummarizer;
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
        this.notificationAccessor = notificationAccessor;
    }

    public final void processNotifications(List<AlertNotificationModel> notifications) {
        processNotifications(notifications, List.of());
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
        List<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(filterableNotifications, frequencies);
        for (FilteredJobNotificationWrapper jobNotificationWrapper : mappedNotifications) {
            List<NotificationContentWrapper> filteredNotifications = jobNotificationWrapper.getJobNotifications();
            Set<Long> notificationIds = filteredNotifications
                                            .stream()
                                            .map(NotificationContentWrapper::getNotificationId)
                                            .collect(Collectors.toSet());

            ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobNotificationWrapper.getJobId(), jobNotificationWrapper.getChannelName(), notificationIds);
            ProviderMessageHolder providerMessageHolder = processJobNotifications(jobNotificationWrapper.getProcessingType(), filteredNotifications);

            providerMessageDistributor.distribute(processedNotificationDetails, providerMessageHolder);
        }
    }

    private ProviderMessageHolder processJobNotifications(ProcessingType processingType, List<NotificationContentWrapper> jobNotifications) {
        ProviderMessageHolder extractedProviderMessages = jobNotifications
                                                              .stream()
                                                              .map(providerMessageExtractionDelegator::extract)
                                                              .reduce(ProviderMessageHolder::reduce)
                                                              .orElse(ProviderMessageHolder.empty());

        return processExtractedNotifications(processingType, extractedProviderMessages);
    }

    private ProviderMessageHolder processExtractedNotifications(ProcessingType processingType, ProviderMessageHolder providerMessages) {
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return providerMessages;
        }

        List<ProjectMessage> digestedMessages = projectMessageDigester.digest(providerMessages.getProjectMessages());
        if (ProcessingType.SUMMARY.equals(processingType)) {
            List<SimpleMessage> summarizedMessages = digestedMessages
                                                         .stream()
                                                         .map(projectMessageSummarizer::summarize)
                                                         .collect(Collectors.toList());
            List<SimpleMessage> allSimpleMessages = ListUtils.union(providerMessages.getSimpleMessages(), summarizedMessages);
            return new ProviderMessageHolder(List.of(), allSimpleMessages);
        }
        return new ProviderMessageHolder(digestedMessages, providerMessages.getSimpleMessages());
    }

    private void clearCaches() {
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }

}
