package com.synopsys.integration.alert.api.distribution.audit;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

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
            .filter(Predicate.not(ExecutingJob::isCompleted))
            .ifPresent(executingJob -> {
                executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.SUCCESS);
                executingJobManager.endJob(jobExecutionId, Instant.ofEpochMilli(event.getCreatedTimestamp()));
            });
    }
}
