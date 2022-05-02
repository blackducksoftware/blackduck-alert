/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.mapping.JobNotificationMap;

@Component
public class ProcessingJobEventHandler implements AlertEventHandler<JobProcessingEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final NotificationContentProcessor notificationContentProcessor;
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;
    private final NotificationAccessor notificationAccessor;
    private final JobAccessor jobAccessor;
    private final JobNotificationMap jobNotificationMap;

    @Autowired
    public ProcessingJobEventHandler(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        NotificationContentProcessor notificationContentProcessor,
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor,
        JobNotificationMap jobNotificationMap
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.notificationContentProcessor = notificationContentProcessor;
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
        this.jobNotificationMap = jobNotificationMap;
    }

    @Override
    public void handle(JobProcessingEvent event) {
        UUID correlationId = event.getCorrelationId();
        UUID jobId = event.getJobId();
        try {
            //TODO have to use paging here.
            List<AlertNotificationModel> notifications = getNotifications(event);
            logNotifications("Start", event, notifications);
            processAndDistribute(jobId, notifications);
            logNotifications("Finished", event, notifications);
        } finally {
            clearCaches();
            jobNotificationMap.removeMapping(correlationId, jobId);
        }
    }

    private List<AlertNotificationModel> getNotifications(JobProcessingEvent event) {
        List<Long> notificationIds = jobNotificationMap.getNotificationsForJob(event.getCorrelationId(), event.getJobId());
        return notificationAccessor.findByIds(notificationIds);
    }

    private void logNotifications(String messagePrefix, JobProcessingEvent event, List<AlertNotificationModel> notifications) {
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = notifications.stream()
                .map(AlertNotificationModel::getId)
                .collect(Collectors.toList());
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug("{} processing batch: {} job: {} notifications: {}", messagePrefix, event.getCorrelationId(), event.getJobId(), joinedIds);
        }
    }

    private void processAndDistribute(UUID jobId, List<AlertNotificationModel> notifications) {
        Optional<DistributionJobModel> jobModel = jobAccessor.getJobById(jobId);
        if (jobModel.isPresent()) {
            DistributionJobModel job = jobModel.get();
            List<NotificationContentWrapper> notificationContentList = notifications
                .stream()
                .map(notificationDetailExtractionDelegator::wrapNotification)
                .flatMap(List::stream)
                .map(DetailedNotificationContent::getNotificationContentWrapper)
                .collect(Collectors.toList());
            ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(
                job.getJobId(),
                job.getChannelDescriptorName(),
                job.getName()
            );
            ProcessedProviderMessageHolder processedMessageHolder = notificationContentProcessor.processNotificationContent(
                job.getProcessingType(),
                notificationContentList
            );

            providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);
        }
    }

    private void clearCaches() {
        // TODO add a task to periodically clean the lifecycle cache if the notification job map is empty
        for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
            lifecycleCache.clear();
        }
    }
}
