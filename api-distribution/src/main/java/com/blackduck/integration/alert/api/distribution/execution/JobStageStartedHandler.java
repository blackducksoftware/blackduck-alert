/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertEventHandler;

@Component
public class JobStageStartedHandler implements AlertEventHandler<JobStageStartedEvent> {
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JobStageStartedHandler(ExecutingJobManager executingJobManager) {
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(JobStageStartedEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        JobStage jobStage = event.getJobStage();
        executingJobManager.getExecutingJob(jobExecutionId)
            .ifPresent(executingJob -> executingJobManager.startStage(jobExecutionId, jobStage, Instant.ofEpochMilli(event.getStartTimeMilliseconds())));
    }
}
