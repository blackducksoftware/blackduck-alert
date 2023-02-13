package com.synopsys.integration.alert.common.persistence.model.job.executions;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class JobExecutionStatusModel extends AlertSerializableModel {
    private static final long serialVersionUID = -118491395692643581L;
    private final UUID jobConfigId;
    private final Long latestNotificationCount;
    private final Long totalNotificationCount;
    private final Long successCount;
    private final Long failureCount;
    private final String latestStatus;
    private final OffsetDateTime lastRun;
    private final JobExecutionStatusDurations durations;

    public JobExecutionStatusModel(
        UUID jobConfigId,
        Long latestNotificationCount,
        Long totalNotificationCount,
        Long successCount,
        Long failureCount,
        String latestStatus,
        OffsetDateTime lastRun,
        JobExecutionStatusDurations durations
    ) {
        this.jobConfigId = jobConfigId;
        this.latestNotificationCount = latestNotificationCount;
        this.totalNotificationCount = totalNotificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
        this.durations = durations;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getLatestNotificationCount() {
        return latestNotificationCount;
    }

    public Long getTotalNotificationCount() {
        return totalNotificationCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }

    public JobExecutionStatusDurations getDurations() {
        return durations;
    }
}
