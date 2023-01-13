package com.synopsys.integration.alert.common.persistence.model.job.executions;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class JobExecutionStatusModel extends AlertSerializableModel {
    private static final long serialVersionUID = -118491395692643581L;
    private final UUID jobConfigId;
    private final Long notificationCount;
    private final Long successCount;
    private final Long failureCount;
    private final AuditEntryStatus latestStatus;
    private final OffsetDateTime lastRun;
    private final JobExecutionStatusDurations durations;

    public JobExecutionStatusModel(
        UUID jobConfigId,
        Long notificationCount,
        Long successCount,
        Long failureCount,
        AuditEntryStatus latestStatus,
        OffsetDateTime lastRun,
        JobExecutionStatusDurations durations
    ) {
        this.jobConfigId = jobConfigId;
        this.notificationCount = notificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
        this.durations = durations;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getNotificationCount() {
        return notificationCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public AuditEntryStatus getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }

    public JobExecutionStatusDurations getDurations() {
        return durations;
    }
}
