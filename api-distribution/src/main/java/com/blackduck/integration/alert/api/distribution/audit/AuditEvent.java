package com.blackduck.integration.alert.api.distribution.audit;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.common.util.DateUtils;

public class AuditEvent extends AlertEvent {
    private static final long serialVersionUID = 8821840075948290969L;
    private final UUID jobExecutionId;
    private final UUID jobConfigId;
    private final Set<Long> notificationIds;
    private final long createdTimestamp;

    public AuditEvent(String destination, UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds) {
        this(destination, jobExecutionId, jobConfigId, notificationIds, DateUtils.createCurrentDateTimestamp().toInstant());
    }

    public AuditEvent(String destination, UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds, Instant createdTimestamp) {
        super(destination);
        this.jobExecutionId = jobExecutionId;
        this.jobConfigId = jobConfigId;
        this.notificationIds = notificationIds;
        this.createdTimestamp = createdTimestamp.toEpochMilli();
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }
}
