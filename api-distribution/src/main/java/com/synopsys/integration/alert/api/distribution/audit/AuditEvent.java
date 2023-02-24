package com.synopsys.integration.alert.api.distribution.audit;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.common.util.DateUtils;

public class AuditEvent extends AlertEvent {
    private static final long serialVersionUID = 8821840075948290969L;
    private final UUID jobExecutionId;
    private final UUID jobConfigId;
    private final Set<Long> notificationIds;
    private final OffsetDateTime createdTimestamp;

    public AuditEvent(String destination, UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds) {
        this(destination, jobExecutionId, jobConfigId, notificationIds, DateUtils.createCurrentDateTimestamp());
    }

    public AuditEvent(String destination, UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds, OffsetDateTime createdTimestamp) {
        super(destination);
        this.jobExecutionId = jobExecutionId;
        this.jobConfigId = jobConfigId;
        this.notificationIds = notificationIds;
        this.createdTimestamp = createdTimestamp;
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

    public OffsetDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }
}
