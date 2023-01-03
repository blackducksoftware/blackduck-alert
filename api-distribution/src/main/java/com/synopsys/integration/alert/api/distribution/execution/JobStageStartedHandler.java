package com.synopsys.integration.alert.api.distribution.execution;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;

@Component
public class JobStageStartedHandler implements AlertEventHandler<JobStageStartedEvent> {
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JobStageStartedHandler(final ExecutingJobManager executingJobManager) {
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(final JobStageStartedEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        JobStage jobStage = event.getJobStage();
        executingJobManager.getExecutingJob(jobExecutionId)
            .ifPresent(executingJob -> executingJobManager.startStage(jobExecutionId, jobStage));
    }
}
