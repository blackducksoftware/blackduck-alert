/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.executions;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JobCompletionStatusModel extends AlertSerializableModel {
    private static final long serialVersionUID = -118491395692643581L;
    private final UUID jobConfigId;
    private final long latestNotificationCount;
    private final long totalNotificationCount;
    private final long successCount;
    private final long failureCount;
    private final String latestStatus;
    private final OffsetDateTime lastRun;
    private final OffsetDateTime firstRun;
    private final JobCompletionStatusDurations durations;

    public JobCompletionStatusModel(
        UUID jobConfigId,
        long latestNotificationCount,
        long totalNotificationCount,
        long successCount,
        long failureCount,
        String latestStatus,
        OffsetDateTime lastRun,
        OffsetDateTime firstRun,
        JobCompletionStatusDurations durations
    ) {
        this.jobConfigId = jobConfigId;
        this.latestNotificationCount = latestNotificationCount;
        this.totalNotificationCount = totalNotificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
        this.firstRun = firstRun;
        this.durations = durations;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public long getLatestNotificationCount() {
        return latestNotificationCount;
    }

    public long getTotalNotificationCount() {
        return totalNotificationCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }
    public OffsetDateTime getFirstRun() { return firstRun; }

    public JobCompletionStatusDurations getDurations() {
        return durations;
    }
}
