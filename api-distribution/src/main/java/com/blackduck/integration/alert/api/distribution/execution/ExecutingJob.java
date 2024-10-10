/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;

public class ExecutingJob {
    private final UUID executionId;
    private final UUID jobConfigId;
    private final Instant start;
    private Instant end;
    private AuditEntryStatus status;
    private final AtomicInteger processedNotificationCount;
    private final AtomicInteger totalNotificationCount;

    private final AtomicInteger remainingEvents;

    private final AtomicInteger expectedNotificationsToSend;
    private final AtomicInteger notificationsSent;

    private final Map<JobStage, ExecutingJobStage> stages = new ConcurrentHashMap<>();

    public static ExecutingJob startJob(UUID jobConfigId, int totalNotificationCount) {
        return new ExecutingJob(jobConfigId, Instant.now(), AuditEntryStatus.PENDING, totalNotificationCount);
    }

    private ExecutingJob(UUID jobConfigId, Instant start, AuditEntryStatus status, int totalNotificationCount) {
        this.executionId = UUID.randomUUID();
        this.jobConfigId = jobConfigId;
        this.start = start;
        this.status = status;
        this.processedNotificationCount = new AtomicInteger(0);
        this.totalNotificationCount = new AtomicInteger(totalNotificationCount);
        this.remainingEvents = new AtomicInteger(0);
        this.expectedNotificationsToSend = new AtomicInteger(0);
        this.notificationsSent = new AtomicInteger(0);
    }

    public void endJob(Instant endTime) {
        synchronized (this) {
            this.end = endTime;
        }
    }

    public void updateStatus(AuditEntryStatus status) {
        synchronized (this) {
            if (AuditEntryStatus.FAILURE == status
                || !hasCompletedStatus()) {
                this.status = status;
            }
        }
    }

    public void updateNotificationCount(int notificationCount) {
        synchronized (this) {
            this.processedNotificationCount.addAndGet(notificationCount);
        }
    }

    public void incrementExpectedNotificationsSent(int notificationCount) {
        synchronized (this) {
            this.expectedNotificationsToSend.addAndGet(notificationCount);
        }
    }

    public void incrementNotificationsSentCount(int notificationCount) {
        synchronized (this) {
            this.notificationsSent.addAndGet(notificationCount);
        }
    }

    public void incrementRemainingEventCount(int eventsToAdd) {
        synchronized (this) {
            this.remainingEvents.addAndGet(eventsToAdd);
        }
    }

    public void decrementRemainingEventCount() {
        synchronized (this) {
            this.remainingEvents.decrementAndGet();
        }
    }

    public void addStage(ExecutingJobStage jobStage) {
        synchronized (this) {
            stages.putIfAbsent(jobStage.getStage(), jobStage);
        }
    }

    public Optional<ExecutingJobStage> getStage(JobStage jobStage) {
        return Optional.ofNullable(stages.getOrDefault(jobStage, null));
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public int getProcessedNotificationCount() {
        return processedNotificationCount.get();
    }

    public int getTotalNotificationCount() {
        return totalNotificationCount.get();
    }

    public int getExpectedNotificationsToSend() {
        return expectedNotificationsToSend.get();
    }

    public int getNotificationsSent() {
        return notificationsSent.get();
    }

    public Instant getStart() {
        return start;
    }

    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return !hasRemainingEvents() && hasCompletedStatus();
    }

    public boolean hasCompletedStatus() {
        return AuditEntryStatus.SUCCESS == getStatus() ||
            AuditEntryStatus.FAILURE == getStatus();
    }

    public int getRemainingEvents() {
        return remainingEvents.get();
    }

    public boolean hasRemainingEvents() {
        return remainingEvents.get() > 0;
    }

    public Map<JobStage, ExecutingJobStage> getStages() {
        return stages;
    }
}
