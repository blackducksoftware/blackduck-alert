package com.synopsys.integration.alert.api.distribution;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.audit.AuditFailedEvent;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.util.AuditStackTraceUtil;

public abstract class JobSubTaskEventHandler<T extends JobSubTaskEvent> implements AlertEventHandler<T> {
    private final EventManager eventManager;
    private final JobStage jobStage;
    private final ExecutingJobManager executingJobManager;

    protected JobSubTaskEventHandler(EventManager eventManager, JobStage jobStage, ExecutingJobManager executingJobManager) {
        this.eventManager = eventManager;
        this.jobStage = jobStage;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public final void handle(T event) {
        UUID jobExecutionId = event.getJobExecutionId();
        executingJobManager.decrementRemainingEvents(jobExecutionId);
        try {
            handleEvent(event);
            if (!executingJobManager.hasRemainingEvents(jobExecutionId)) {
                executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
                executingJobManager.getExecutingJob(jobExecutionId)
                    .filter(Predicate.not(ExecutingJob::isCompleted))
                    .ifPresent(executingJob -> {
                        executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.SUCCESS);
                        executingJobManager.endJob(jobExecutionId, Instant.now());
                    });
            }
        } catch (AlertException exception) {
            executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
            executingJobManager.incrementSentNotificationCount(jobExecutionId, event.getNotificationIds().size());
            eventManager.sendEvent(new AuditFailedEvent(
                jobExecutionId,
                event.getJobId(),
                event.getNotificationIds(),
                exception.getMessage(),
                AuditStackTraceUtil.createStackTraceString(exception)
            ));
        }
    }

    protected abstract void handleEvent(T event) throws AlertException;

    protected void postEvents(List<AlertEvent> alertEvent) {
        eventManager.sendEvents(alertEvent);
    }
}
