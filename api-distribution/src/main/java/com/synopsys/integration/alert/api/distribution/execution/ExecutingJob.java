package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class ExecutingJob {
    private final UUID executionId;
    private final UUID jobConfigId;
    private final Instant start;
    private Instant end;
    private AuditEntryStatus status;
    private final AtomicInteger processedNotificationCount;
    private final AtomicInteger totalNotificationCount;

    private final AtomicInteger remainingEvents;
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
    }

    public void jobSucceeded(Instant endTime) {
        completeJobWithStatus(AuditEntryStatus.SUCCESS, endTime);
    }

    public void jobFailed(Instant endTime) {
        completeJobWithStatus(AuditEntryStatus.FAILURE, endTime);
    }

    private void completeJobWithStatus(AuditEntryStatus status, Instant endTime) {
        this.end = endTime;
        this.status = status;
    }

    public void updateNotificationCount(int notificationCount) {
        this.processedNotificationCount.addAndGet(notificationCount);
    }

    public void incrementRemainingEventCount(int eventsToAdd) {
        this.remainingEvents.addAndGet(eventsToAdd);
    }

    public void decrementRemainingEventCount() {
        this.remainingEvents.decrementAndGet();
    }

    public void addStage(ExecutingJobStage jobStage) {
        stages.putIfAbsent(jobStage.getStage(), jobStage);
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

    public Instant getStart() {
        return start;
    }

    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public int getRemainingEvents() {
        return remainingEvents.get();
    }

    public Map<JobStage, ExecutingJobStage> getStages() {
        return stages;
    }
}
