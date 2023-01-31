package com.synopsys.integration.alert.api.distribution.audit;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;

@Component
public class AuditSuccessHandler implements AlertEventHandler<AuditSuccessEvent> {
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public AuditSuccessHandler(ExecutingJobManager executingJobManager) {
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(AuditSuccessEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        executingJobManager.getExecutingJob(jobExecutionId)
            .filter(ExecutingJob::isCompleted)
            .ifPresent(executingJob -> executingJobManager.endJobWithSuccess(jobExecutionId, event.getCreatedTimestamp().toInstant()));
    }
}
