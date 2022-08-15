package com.synopsys.integration.alert.api.distribution;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.common.util.DateUtils;

public class AuditEvent extends AlertEvent {
    private static final long serialVersionUID = 8821840075948290969L;
    private final UUID jobId;
    private final Set<Long> notificationIds;
    private OffsetDateTime createdTimestamp;

    public AuditEvent(String destination, UUID jobId, Set<Long> notificationIds) {
        this(destination, jobId, notificationIds, DateUtils.createCurrentDateTimestamp());
    }

    public AuditEvent(String destination, UUID jobId, Set<Long> notificationIds, OffsetDateTime createdTimestamp) {
        super(destination);
        this.jobId = jobId;
        this.notificationIds = notificationIds;
        this.createdTimestamp = createdTimestamp;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public OffsetDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }
}
