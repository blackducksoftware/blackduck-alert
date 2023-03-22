/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.processor.api.JobNotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;

@Component
public class ProcessingJobEventHandler implements AlertEventHandler<JobProcessingEvent> {
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
            }
        } finally {
            jobNotificationMappingAccessor.removeJobMapping(correlationId, jobId);
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
