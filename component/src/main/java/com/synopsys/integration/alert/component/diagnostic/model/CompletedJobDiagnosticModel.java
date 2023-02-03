package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class CompletedJobDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = -4662389084967372263L;
    private final UUID jobConfigId;
    private final String jobName;
    private final Long latestNotificationCount;
    private final Long totalNotificationCount;
    private final Long successCount;
    private final Long failureCount;
    private final String latestStatus;
    private final String lastRun;

    private final CompletedJobDurationDiagnosticModel durations;

    public CompletedJobDiagnosticModel(
        UUID jobConfigId,
        String jobName,
        Long latestNotificationCount,
        Long totalNotificationCount,
        Long successCount,
        Long failureCount,
        String latestStatus,
        String lastRun,
        CompletedJobDurationDiagnosticModel durations
    ) {
        this.jobConfigId = jobConfigId;
        this.jobName = jobName;
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

    public String getJobName() {
        return jobName;
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

    public String getLastRun() {
        return lastRun;
    }

    public CompletedJobDurationDiagnosticModel getDurations() {
        return durations;
    }
}

