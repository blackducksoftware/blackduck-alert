package com.synopsys.integration.alert.api.distribution.audit;

import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;

@Component
public class AuditFailedHandler implements AlertEventHandler<AuditFailedEvent> {
    private final ProcessingFailedAccessor processingFailedAccessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public AuditFailedHandler(
        ProcessingFailedAccessor processingFailedAccessor,
        ExecutingJobManager executingJobManager
    ) {
        this.processingFailedAccessor = processingFailedAccessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(AuditFailedEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        UUID jobConfigId = event.getJobConfigId();
        synchronized (this) {
            if (event.getStackTrace().isPresent()) {
                processingFailedAccessor.setAuditFailure(
                    jobExecutionId,
                    jobConfigId,
                    event.getNotificationIds(),
                    event.getCreatedTimestamp(),
                    event.getErrorMessage(),
                    event.getStackTrace().orElse("NO STACK TRACE")
                );
            } else {
                processingFailedAccessor.setAuditFailure(jobExecutionId, jobConfigId, event.getNotificationIds(), event.getCreatedTimestamp(), event.getErrorMessage());
            }
        }
        executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.FAILURE);
        executingJobManager.getExecutingJob(jobExecutionId)
            .filter(Predicate.not(ExecutingJob::hasRemainingEvents))
            .ifPresent(executingJob -> executingJobManager.endJob(jobExecutionId, event.getCreatedTimestamp().toInstant()));
    }
}
