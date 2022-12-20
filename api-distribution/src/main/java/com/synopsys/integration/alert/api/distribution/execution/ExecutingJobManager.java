package com.synopsys.integration.alert.api.distribution.execution;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

@Component
public class ExecutingJobManager {
    private final Map<UUID, ExecutingJob> executingJobMap = new ConcurrentHashMap<>();

    public ExecutingJob startJob(UUID jobConfigId) {
        ExecutingJob job = ExecutingJob.startJob(jobConfigId);
        executingJobMap.putIfAbsent(job.getExecutionId(), job);
        return job;
    }

    public Optional<ExecutingJob> endJobWithSuccess(UUID executionId) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(ExecutingJob::jobSucceeded);
        return executingJob;
    }

    public Optional<ExecutingJob> endJobWithFailure(UUID executionId) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(ExecutingJob::jobFailed);
        return executingJob;
    }

    public Optional<ExecutingJob> getExecutingJob(UUID jobExecutionId) {
        return Optional.ofNullable(executingJobMap.getOrDefault(jobExecutionId, null));
    }

    public boolean startStage(UUID executionId, JobStage stage) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(job -> {
            job.addStage(ExecutingJobStage.createStage(executionId, stage));
        });
        return executingJob.isPresent();
    }

    public boolean endStage(UUID executionId, JobStage stage) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob
            .flatMap(job -> job.getStage(stage))
            .ifPresent(ExecutingJobStage::endStage);
        return executingJob.isPresent();
    }

    public AggregatedExecutionResults aggregateExecutingJobData() {
        Long pendingCount = countPendingJobs();
        Long successCount = countSuccessfulJobs();
        Long failedJobs = countFailedJobs();
        Long totalJobs = Long.valueOf(executingJobMap.size());

        return new AggregatedExecutionResults(totalJobs, pendingCount, successCount, failedJobs);
    }

    private Long countSuccessfulJobs() {
        return countJobsByStatus(AuditEntryStatus.SUCCESS);
    }

    private Long countFailedJobs() {
        return countJobsByStatus(AuditEntryStatus.FAILURE);
    }

    private Long countPendingJobs() {
        return countJobsByStatus(AuditEntryStatus.PENDING);
    }

    private Long countJobsByStatus(AuditEntryStatus entryStatus) {
        return executingJobMap.values().stream()
            .filter(executingJob -> executingJob.getStatus().equals(entryStatus))
            .count();

    }
}
