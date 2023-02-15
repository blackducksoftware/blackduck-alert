package com.synopsys.integration.alert.api.event.distribution;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class JobSubTaskEvent extends AlertEvent {
    private static final long serialVersionUID = 2328435266614582583L;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    protected JobSubTaskEvent(String destination, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        super(destination);
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    public UUID getParentEventId() {
        return parentEventId;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }
}
