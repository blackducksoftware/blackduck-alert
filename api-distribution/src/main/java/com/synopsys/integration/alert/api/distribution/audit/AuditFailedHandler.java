package com.synopsys.integration.alert.api.distribution.audit;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
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
        executingJobManager.endJobWithFailure(jobExecutionId, event.getCreatedTimestamp().toInstant());
        Optional<ExecutingJob> executingJobOptional = executingJobManager.getExecutingJob(jobExecutionId);
        if (executingJobOptional.isPresent()) {
            ExecutingJob executingJob = executingJobOptional.get();
            UUID jobConfigId = executingJob.getJobConfigId();
            if (event.getStackTrace().isPresent()) {
                processingFailedAccessor.setAuditFailure(
                    jobConfigId,
                    event.getNotificationIds(),
                    event.getCreatedTimestamp(),
                    event.getErrorMessage(),
                    event.getStackTrace().orElse("NO STACK TRACE")
                );
            } else {
                processingFailedAccessor.setAuditFailure(jobConfigId, event.getNotificationIds(), event.getCreatedTimestamp(), event.getErrorMessage());
            }
        }
    }
}
