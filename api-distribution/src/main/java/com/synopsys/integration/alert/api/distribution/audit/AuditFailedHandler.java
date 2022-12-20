package com.synopsys.integration.alert.api.distribution.audit;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

@Component
public class AuditFailedHandler implements AlertEventHandler<AuditFailedEvent> {
    private final ProcessingAuditAccessor processingAuditAccessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public AuditFailedHandler(ProcessingAuditAccessor processingAuditAccessor, ExecutingJobManager executingJobManager) {
        this.processingAuditAccessor = processingAuditAccessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(AuditFailedEvent event) {
        executingJobManager.endJobWithFailure(event.getJobExecutionId());
        executingJobManager.getExecutingJob(event.getJobExecutionId()).ifPresent(executingJob -> {
            UUID jobConfigId = executingJob.getJobConfigId();
            Set<Long> notificationids = event.getNotificationIds();
            processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(jobConfigId, notificationids);
            processingAuditAccessor.setAuditEntryFailure(
                jobConfigId,
                event.getNotificationIds(),
                event.getCreatedTimestamp(),
                event.getErrorMessage(),
                event.getStackTrace().orElse(null)
            );
        });
    }
}
