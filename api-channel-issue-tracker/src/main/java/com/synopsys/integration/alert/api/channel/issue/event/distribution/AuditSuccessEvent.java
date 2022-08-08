package com.synopsys.integration.alert.api.channel.issue.event.distribution;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class AuditSuccessEvent extends AlertEvent {

    private final UUID jobId;
    private final Set<Long> notificationIds;

    public AuditSuccessEvent(String destination, UUID jobId, Set<Long> notificationIds) {
        super(destination);
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }
}
