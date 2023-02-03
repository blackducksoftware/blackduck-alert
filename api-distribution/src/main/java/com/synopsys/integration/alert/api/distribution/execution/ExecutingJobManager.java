package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class ExecutingJobManager {
    private final Map<UUID, ExecutingJob> executingJobMap = new ConcurrentHashMap<>();
    private final JobExecutionStatusAccessor jobCompletionStatusAccessor;

    @Autowired
    public ExecutingJobManager(JobExecutionStatusAccessor jobCompletionStatusAccessor) {
        this.jobCompletionStatusAccessor = jobCompletionStatusAccessor;
    }

    public ExecutingJob startJob(UUID jobConfigId, int totalNotificationCount) {
        ExecutingJob job = ExecutingJob.startJob(jobConfigId, totalNotificationCount);
        executingJobMap.putIfAbsent(job.getExecutionId(), job);
        return job;
    }

    public void endJobWithSuccess(UUID executionId, Instant endTime) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(execution -> {
            execution.jobSucceeded(DateUtils.fromInstantUTC(endTime).toInstant());
            jobCompletionStatusAccessor.saveExecutionStatus(createStatusModel(execution, AuditEntryStatus.SUCCESS));
            purgeJob(executionId);
        });
    }

    public void endJobWithFailure(UUID executionId, Instant endTime) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(execution -> {
            execution.jobFailed(DateUtils.fromInstantUTC(endTime).toInstant());
            jobCompletionStatusAccessor.saveExecutionStatus(createStatusModel(execution, AuditEntryStatus.FAILURE));
            purgeJob(executionId);
        });
    }

    public void incrementProcessedNotificationCount(UUID jobExecutionId, int notificationCount) {
        Optional<ExecutingJob> executingJob = getExecutingJob(jobExecutionId);
        executingJob.ifPresent(execution -> execution.updateNotificationCount(notificationCount));
    }

    public Optional<ExecutingJob> getExecutingJob(UUID jobExecutionId) {
        return Optional.ofNullable(executingJobMap.getOrDefault(jobExecutionId, null));
    }

    public AlertPagedModel<ExecutingJob> getExecutingJobs(int pageNumber, int pageSize) {
        List<List<ExecutingJob>> pages = ListUtils.partition(new ArrayList<>(executingJobMap.values()), pageSize);
        List<ExecutingJob> pageOfData = List.of();
        if (!pages.isEmpty() && pageNumber - 1 < pages.size()) {
            pageOfData = pages.get(pageNumber - 1);
        }
        return new AlertPagedModel<>(pages.size(), pageNumber, pageSize, pageOfData);
    }

    public void startStage(UUID executionId, JobStage stage, Instant start) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(job -> {
            job.addStage(ExecutingJobStage.createStage(executionId, stage, DateUtils.fromInstantUTC(start).toInstant()));
        });
    }

    public void endStage(UUID executionId, JobStage stage, Instant end) {
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob
            .flatMap(job -> job.getStage(stage))
            .ifPresent(jobStage -> jobStage.endStage(DateUtils.fromInstantUTC(end).toInstant()));
    }

    public void purgeJob(UUID executionId) {
        executingJobMap.remove(executionId);
    }

    public void incrementRemainingEvents(UUID jobExecutionId, int eventCount) {
        getExecutingJob(jobExecutionId)
            .ifPresent(executingJob -> executingJob.incrementRemainingEventCount(eventCount));
    }

    public void decrementRemainingEvents(UUID jobExecutionId) {
        getExecutingJob(jobExecutionId)
            .ifPresent(ExecutingJob::decrementRemainingEventCount);
    }

    public boolean hasRemainingEvents(UUID jobExecutionId) {
        return getExecutingJob(jobExecutionId)
            .map(ExecutingJob::getRemainingEvents)
            .stream().anyMatch(remainingEventCount -> remainingEventCount > 0);
    }

    public void incrementSentNotificationCount(UUID jobExecutionId, int notificationCount) {
        Optional<ExecutingJob> executingJob = getExecutingJob(jobExecutionId);
        executingJob.ifPresent(execution -> execution.incrementNotificationsSentCount(notificationCount));
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

    private JobExecutionStatusModel createStatusModel(ExecutingJob executingJob, AuditEntryStatus jobStatus) {

        UUID jobConfigId = executingJob.getJobConfigId();
        long successCount = AuditEntryStatus.SUCCESS == jobStatus ? 1L : 0L;
        long failureCount = AuditEntryStatus.FAILURE == jobStatus ? 1L : 0L;

        JobExecutionStatusDurations durations = new JobExecutionStatusDurations(
            calculateNanosecondDuration(executingJob.getStart(), executingJob.getEnd().orElse(Instant.now())),
            calculateJobStageDuration(executingJob, JobStage.NOTIFICATION_PROCESSING),
            calculateJobStageDuration(executingJob, JobStage.CHANNEL_PROCESSING),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_CREATION),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_COMMENTING),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_TRANSITION)
        );

        return new JobExecutionStatusModel(
            jobConfigId,
            Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue(),
            Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue(),
            successCount,
            failureCount,
            jobStatus.name(),
            executingJob.getEnd().map(DateUtils::fromInstantUTC).orElse(null),
            durations
        );
    }

    private Long calculateJobStageDuration(ExecutingJob executingJob, JobStage stage) {
        return executingJob.getStage(stage)
            .filter(executingJobStage -> executingJobStage.getEnd().isPresent())
            .map(executedStage -> calculateNanosecondDuration(executedStage.getStart(), executedStage.getEnd().orElse(Instant.now())))
            .orElse(0L);
    }

    private Long calculateNanosecondDuration(Instant start, Instant end) {
        return Duration.between(start, end).toNanos();
    }
}
