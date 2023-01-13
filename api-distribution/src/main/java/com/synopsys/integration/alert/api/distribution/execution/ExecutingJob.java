package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class ExecutingJob {
    private final UUID executionId;
    private final UUID jobConfigId;
    private final Instant start;
    private Instant end;
    private AuditEntryStatus status;
    private int processedNotificationCount;
    private final int totalNotificationCount;
    private final Map<JobStage, ExecutingJobStage> stages = new ConcurrentHashMap<>();

    public static ExecutingJob startJob(UUID jobConfigId, int totalNotificationCount) {
        return new ExecutingJob(jobConfigId, Instant.now(), AuditEntryStatus.PENDING, totalNotificationCount);
    }

    private ExecutingJob(UUID jobConfigId, Instant start, AuditEntryStatus status, int totalNotificationCount) {
        this.executionId = UUID.randomUUID();
        this.jobConfigId = jobConfigId;
        this.start = start;
        this.status = status;
        this.processedNotificationCount = 0;
        this.totalNotificationCount = totalNotificationCount;
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
        this.processedNotificationCount += notificationCount;
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
        return processedNotificationCount;
    }

    public int getTotalNotificationCount() {
        return totalNotificationCount;
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

    public Map<JobStage, ExecutingJobStage> getStages() {
        return stages;
    }
}
