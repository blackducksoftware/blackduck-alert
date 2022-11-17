package com.synopsys.integration.alert.api.distribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.audit.AuditFailedEvent;
import com.synopsys.integration.alert.api.distribution.audit.AuditSuccessEvent;
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

    protected JobSubTaskEventHandler(EventManager eventManager, JobSubTaskAccessor jobSubTaskAccessor) {
        this.eventManager = eventManager;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
    }

    @Override
    public final void handle(T event) {
        UUID parentEventId = event.getParentEventId();
        try {
            handleEvent(event);
            Optional<JobSubTaskStatusModel> subTaskStatus = jobSubTaskAccessor.decrementTaskCount(parentEventId);
            subTaskStatus.map(JobSubTaskStatusModel::getRemainingTaskCount)
                .filter(remainingCount -> remainingCount < 1)
                .ifPresent(ignored -> {
                    eventManager.sendEvent(new AuditSuccessEvent(event.getJobId(), event.getNotificationIds()));
                    jobSubTaskAccessor.removeSubTaskStatus(parentEventId);
                });
        } catch (AlertException exception) {
            eventManager.sendEvent(new AuditFailedEvent(
                event.getJobId(),
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
