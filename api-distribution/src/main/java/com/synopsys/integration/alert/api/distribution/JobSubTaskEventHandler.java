package com.synopsys.integration.alert.api.distribution;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.common.persistence.util.AuditStackTraceUtil;

public abstract class JobSubTaskEventHandler<T extends JobSubTaskEvent> implements AlertEventHandler<T> {
    private final EventManager eventManager;
    private final JobSubTaskAccessor jobSubTaskAccessor;
    private final JobStage jobStage;
    private final ExecutingJobManager executingJobManager;

    protected JobSubTaskEventHandler(EventManager eventManager, JobSubTaskAccessor jobSubTaskAccessor, JobStage jobStage, ExecutingJobManager executingJobManager) {
        this.eventManager = eventManager;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
        this.jobStage = jobStage;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public final void handle(T event) {
        UUID parentEventId = event.getParentEventId();
        UUID jobExecutionId = event.getJobExecutionId();
        executingJobManager.decrementRemainingEvents(jobExecutionId);
        try {
            handleEvent(event);
            Optional<JobSubTaskStatusModel> subTaskStatus = jobSubTaskAccessor.decrementTaskCount(parentEventId);
            subTaskStatus.map(JobSubTaskStatusModel::getRemainingTaskCount)
                .filter(remainingCount -> remainingCount < 1)
                .ifPresent(ignored -> {
                    //TODO no longer need this component
                    jobSubTaskAccessor.removeSubTaskStatus(parentEventId);
                });
            executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
            executingJobManager.getExecutingJob(jobExecutionId)
                .filter(Predicate.not(ExecutingJob::isCompleted))
                .ifPresent(executingJob -> executingJobManager.endJobWithSuccess(jobExecutionId, Instant.now()));
        } catch (AlertException exception) {
            executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
            //eventManager.sendEvent(new JobStageEndedEvent(jobExecutionId, jobStage, Instant.now().toEpochMilli()));
            eventManager.sendEvent(new AuditFailedEvent(
                jobExecutionId,
                event.getNotificationIds(),
                exception.getMessage(),
                AuditStackTraceUtil.createStackTraceString(exception)
            ));

            jobSubTaskAccessor.removeSubTaskStatus(parentEventId);
        }
    }

    protected abstract void handleEvent(T event) throws AlertException;

    protected void postEvents(List<AlertEvent> alertEvent) {
        eventManager.sendEvents(alertEvent);
    }
}
