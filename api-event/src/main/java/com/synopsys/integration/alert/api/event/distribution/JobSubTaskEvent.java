package com.synopsys.integration.alert.api.event.distribution;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class JobSubTaskEvent extends AlertEvent {
    private final UUID parentEventId;
    private final UUID jobId;
    private final List<Long> notificationIds;

    protected JobSubTaskEvent(String destination, UUID parentEventId, UUID jobId, List<Long> notificationIds) {
        super(destination);
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    public UUID getParentEventId() {
        return parentEventId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }
}
