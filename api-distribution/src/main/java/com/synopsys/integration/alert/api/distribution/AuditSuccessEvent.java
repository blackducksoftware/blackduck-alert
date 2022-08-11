package com.synopsys.integration.alert.api.distribution;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class AuditSuccessEvent extends AlertEvent {
    private static final long serialVersionUID = -2708967824272053739L;

    public static final String DEFAULT_DESTINATION_NAME = "audit_success_event";
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public AuditSuccessEvent(UUID jobId, Set<Long> notificationIds) {
        super(DEFAULT_DESTINATION_NAME);
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
