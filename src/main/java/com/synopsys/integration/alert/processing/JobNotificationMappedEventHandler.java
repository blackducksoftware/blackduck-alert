/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processing;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.processor.api.event.JobNotificationMappedEvent;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;

@Component
public class JobNotificationMappedEventHandler implements AlertEventHandler<JobNotificationMappedEvent> {
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());
    private final JobNotificationMappingAccessor jobMappingAccessor;
    private final EventManager eventManager;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JobNotificationMappedEventHandler(JobNotificationMappingAccessor jobMappingAccessor, EventManager eventManager, ExecutingJobManager executingJobManager) {
        this.jobMappingAccessor = jobMappingAccessor;
        this.eventManager = eventManager;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(JobNotificationMappedEvent event) {
        UUID correlationId = event.getCorrelationId();
        Set<UUID> jobConfigIds = jobMappingAccessor.getUniqueJobIds(correlationId);
        for (UUID jobConfigId : jobConfigIds) {
            ExecutingJob startedJob = executingJobManager.startJob(jobConfigId);
            notificationLogger.info("Creating processing event for jobConfigId: {}, batch: {}", jobConfigId, correlationId);
            notificationLogger.info("Started Job execution: {}", startedJob.getExecutionId());
            eventManager.sendEvent(new JobProcessingEvent(correlationId, jobConfigId, startedJob.getExecutionId()));
        }
    }
}
