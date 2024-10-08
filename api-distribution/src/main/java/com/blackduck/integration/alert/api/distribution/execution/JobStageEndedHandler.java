package com.blackduck.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertEventHandler;

@Component
public class JobStageEndedHandler implements AlertEventHandler<JobStageEndedEvent> {
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JobStageEndedHandler(final ExecutingJobManager executingJobManager) {
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(JobStageEndedEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        JobStage jobStage = event.getJobStage();
        executingJobManager.getExecutingJob(jobExecutionId)
            .ifPresent(executingJob -> executingJobManager.endStage(jobExecutionId, jobStage, Instant.ofEpochMilli(event.endTimeMilliseconds)));
    }
}
