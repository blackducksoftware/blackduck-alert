/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.processor.JobNotificationContentProcessor;
import com.blackduck.integration.alert.api.processor.NotificationProcessingLifecycleCache;
import com.blackduck.integration.alert.api.processor.distribute.ProcessedNotificationDetails;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;

@Component
public class ProcessingJobEventHandler implements AlertEventHandler<JobProcessingEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProviderMessageDistributor providerMessageDistributor;
    private final List<NotificationProcessingLifecycleCache> lifecycleCaches;
    private final JobAccessor jobAccessor;
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;
    private final JobNotificationContentProcessor jobNotificationContentProcessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public ProcessingJobEventHandler(
        ProviderMessageDistributor providerMessageDistributor,
        List<NotificationProcessingLifecycleCache> lifecycleCaches,
        JobAccessor jobAccessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor,
        JobNotificationContentProcessor jobNotificationContentProcessor,
        ExecutingJobManager executingJobManager
    ) {
        this.providerMessageDistributor = providerMessageDistributor;
        this.lifecycleCaches = lifecycleCaches;
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
                executingJobManager.startStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
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
                executingJobManager.endStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
            } else {
                logger.debug("No job discovered for jobId: {}", jobId);
            }
        } finally {
            //TODO: Removed for test purposes
            //jobNotificationMappingAccessor.removeJobMapping(correlationId, jobId);
            clearCaches(correlationId);
        }
    }

    private void clearCaches(UUID correlationId) {
        if (!jobNotificationMappingAccessor.hasJobMappings(correlationId)) {
            for (NotificationProcessingLifecycleCache lifecycleCache : lifecycleCaches) {
                lifecycleCache.clear();
            }
        }
    }
}
