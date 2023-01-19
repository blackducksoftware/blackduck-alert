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

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.processor.api.JobNotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;

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
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;
    private final JobNotificationContentProcessor jobNotificationContentProcessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public ProcessingJobEventHandler(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        NotificationContentProcessor notificationContentProcessor,
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor,
        JobNotificationContentProcessor jobNotificationContentProcessor,
        ExecutingJobManager executingJobManager
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.notificationContentProcessor = notificationContentProcessor;
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
        this.jobNotificationMappingAccessor = jobNotificationMappingAccessor;
        this.jobNotificationContentProcessor = jobNotificationContentProcessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(JobProcessingEvent event) {
        UUID correlationId = event.getCorrelationId();
        UUID jobId = event.getJobId();
        try {
            Optional<DistributionJobModel> jobModel = jobAccessor.getJobById(jobId);
            if (jobModel.isPresent()) {
                int totalNotificationCount = jobNotificationMappingAccessor.getNotificationCountForJob(correlationId, jobId);
                ExecutingJob executingJob = executingJobManager.startJob(jobId, totalNotificationCount);
                executingJobManager.startStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
                DistributionJobModel jobConfiguration = jobModel.get();
                ProcessedProviderMessageHolder processedMessageHolder = jobNotificationContentProcessor.processNotifications(
                    event,
                    executingJob.getExecutionId(),
                    jobConfiguration
                );
                ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(
                    executingJob.getExecutionId(),
                    jobConfiguration.getJobId(),
                    jobConfiguration.getChannelDescriptorName(),
                    jobConfiguration.getName()
                );
                providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);
                executingJobManager.endStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
            }
        } finally {
            jobNotificationMappingAccessor.removeJobMapping(correlationId, jobId);
            clearCaches(correlationId);
        }
    }

    //TODO remove this code...
    private ProcessedProviderMessageHolder processNotifications(JobProcessingEvent event, UUID jobExecutionId, DistributionJobModel job) {
        ProcessedProviderMessageHolder processedMessageHolder = null;
        UUID correlationId = event.getCorrelationId();
        UUID jobId = event.getJobId();
        int pageNumber = 0;
        int pageSize = 200;
        AlertPagedModel<JobToNotificationMappingModel> jobNotificationMappings = jobNotificationMappingAccessor.getJobNotificationMappings(
            correlationId,
            jobId,
            pageNumber,
            pageSize
        );

        while (jobNotificationMappings.getCurrentPage() <= jobNotificationMappings.getTotalPages()) {
            List<Long> notificationIds = extractNotificationIds(jobNotificationMappings);
            List<AlertNotificationModel> notifications = notificationAccessor.findByIds(notificationIds);
            executingJobManager.incrementNotificationCount(jobExecutionId, notifications.size());
            logNotifications("Start", event, notificationIds);
            ProcessedProviderMessageHolder currentProcessedMessages = processNotifications(job, notifications);
            if (null == processedMessageHolder) {
                processedMessageHolder = currentProcessedMessages;
            } else {
                processedMessageHolder = ProcessedProviderMessageHolder.reduce(processedMessageHolder, currentProcessedMessages);
            }
            pageNumber++;
            jobNotificationMappings = jobNotificationMappingAccessor.getJobNotificationMappings(
                correlationId,
                jobId,
                pageNumber,
                pageSize
            );
            logNotifications("Finished", event, notificationIds);
        }

        return processedMessageHolder;
    }

    private List<Long> extractNotificationIds(AlertPagedModel<JobToNotificationMappingModel> pageOfMappingData) {
        return pageOfMappingData.getModels().stream()
            .map(JobToNotificationMappingModel::getNotificationId)
            .collect(Collectors.toList());
    }

    private void logNotifications(String messagePrefix, JobProcessingEvent event, List<Long> notificationIds) {
        if (logger.isDebugEnabled()) {
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug(
                "{} processing job: {} batch: {} {} notifications: {}",
                messagePrefix,
                event.getJobId(),
                event.getCorrelationId(),
                notificationIds.size(),
                joinedIds
            );
        }
    }

    private ProcessedProviderMessageHolder processNotifications(DistributionJobModel jobModel, List<AlertNotificationModel> notifications) {
        List<NotificationContentWrapper> notificationContentList = notifications
            .stream()
            .map(notificationDetailExtractionDelegator::wrapNotification)
            .flatMap(List::stream)
            .map(DetailedNotificationContent::getNotificationContentWrapper)
            .collect(Collectors.toList());
        return notificationContentProcessor.processNotificationContent(
            jobModel.getProcessingType(),
            notificationContentList
        );
    }

    private void clearCaches(UUID correlationId) {
        if (!jobNotificationMappingAccessor.hasJobMappings(correlationId)) {
            for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
                lifecycleCache.clear();
            }
        }
    }
}
