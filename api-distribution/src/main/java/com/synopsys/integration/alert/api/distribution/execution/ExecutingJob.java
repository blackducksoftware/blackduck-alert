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
    private Long notificationCount;
    private final Map<JobStage, ExecutingJobStage> stages = new ConcurrentHashMap<>();

    public static ExecutingJob startJob(UUID jobConfigId) {
        return new ExecutingJob(jobConfigId, Instant.now(), AuditEntryStatus.PENDING);
    }

    private ExecutingJob(UUID jobConfigId, Instant start, AuditEntryStatus status) {
        this.executionId = UUID.randomUUID();
        this.jobConfigId = jobConfigId;
        this.start = start;
        this.status = status;
        this.notificationCount = 0L;
    }

    public void jobSucceeded() {
        completeJobWithStatus(AuditEntryStatus.SUCCESS);
    }

    public void jobFailed() {
        completeJobWithStatus(AuditEntryStatus.FAILURE);
    }

    private void completeJobWithStatus(AuditEntryStatus status) {
        this.end = Instant.now();
        this.status = status;
    }

    public Long updateNotificationCount(Number notificationCount) {
        this.notificationCount += notificationCount.longValue();
        return this.notificationCount;
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

    public Long getNotificationCount() {
        return notificationCount;
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
