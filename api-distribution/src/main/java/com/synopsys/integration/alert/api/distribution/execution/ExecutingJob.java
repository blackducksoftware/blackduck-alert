package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class ExecutingJob {
    private final UUID jobId;
    private final Instant start;
    private Instant end;
    private AuditEntryStatus status;
    private final Map<JobStage, ExecutingJobStage> stages = new ConcurrentHashMap<>();

    public static ExecutingJob startJob(UUID jobId) {
        return new ExecutingJob(jobId, Instant.now(), AuditEntryStatus.PENDING);
    }

    private ExecutingJob(UUID jobId, Instant start, AuditEntryStatus status) {
        this.jobId = jobId;
        this.start = start;
        this.status = status;
    }

    public void completeJobWithStatus(AuditEntryStatus status) {
        this.end = Instant.now();
        this.status = status;
    }

    public void addStage(ExecutingJobStage jobStage) {
        stages.putIfAbsent(jobStage.getStage(), jobStage);
    }

    public Optional<ExecutingJobStage> getStage(JobStage jobStage) {
        return Optional.ofNullable(stages.getOrDefault(jobStage, null));
    }

    public UUID getJobId() {
        return jobId;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public Map<JobStage, ExecutingJobStage> getStages() {
        return stages;
    }
}
