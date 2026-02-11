/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusDurations;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.util.DateUtils;

@Component
public class ExecutingJobManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<UUID, ExecutingJob> executingJobMap = new ConcurrentHashMap<>();
    private final JobCompletionStatusModelAccessor jobCompletionStatusAccessor;

    @Autowired
    public ExecutingJobManager(JobCompletionStatusModelAccessor jobCompletionStatusAccessor) {
        this.jobCompletionStatusAccessor = jobCompletionStatusAccessor;
    }

    public ExecutingJob startJob(UUID jobConfigId, int totalNotificationCount) {
        logger.debug("Starting job for config: {} ({} notifications)", jobConfigId, totalNotificationCount);
        ExecutingJob job = ExecutingJob.startJob(jobConfigId, totalNotificationCount);
        executingJobMap.putIfAbsent(job.getExecutionId(), job);
        Optional<JobCompletionStatusModel> jobExecutionStatusModel = jobCompletionStatusAccessor.getJobExecutionStatus(jobConfigId);
        if (jobExecutionStatusModel.isEmpty()) {
            jobCompletionStatusAccessor.saveExecutionStatus(createEmptyStatusModel(jobConfigId));
        }
        logger.debug("Started job execution: {}", job.getExecutionId());

        return job;
    }

    public void endJob(UUID executionId, Instant endTime) {
        String endTimeDateFormatted = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(endTime));
        logger.debug("Ending job execution {} at {}", executionId, endTimeDateFormatted);
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(execution -> {
            execution.endJob(DateUtils.fromInstantUTC(endTime).toInstant());
            jobCompletionStatusAccessor.saveExecutionStatus(createStatusModel(execution));
            purgeJob(executionId);
        });
    }

    public void updateJobStatus(UUID executionId, AuditEntryStatus status) {
        String statusName = status.name();
        logger.debug("Updating status for job execution {} to {}", executionId, statusName);
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(execution -> execution.updateStatus(status));
    }

    public void incrementProcessedNotificationCount(UUID jobExecutionId, int notificationCount) {
        logger.debug("Incrementing processed notifications for job execution {} by {} notification(s)", jobExecutionId, notificationCount);
        Optional<ExecutingJob> executingJob = getExecutingJob(jobExecutionId);
        executingJob.ifPresent(execution -> execution.updateNotificationCount(notificationCount));
    }

    public Optional<ExecutingJob> getExecutingJob(UUID jobExecutionId) {
        if (executingJobMap.containsKey(jobExecutionId)) {
            return Optional.of(executingJobMap.get(jobExecutionId));
        }
        return Optional.empty();
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
        String startTimeDateFormatted = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(start));
        logger.debug("Starting stage {} for job execution {} at {}", stage, executionId, startTimeDateFormatted);
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob.ifPresent(job -> job.addStage(ExecutingJobStage.createStage(executionId, stage, DateUtils.fromInstantUTC(start).toInstant())));
        executingJob.ifPresent(execution -> jobCompletionStatusAccessor.saveExecutionStatus(createStatusModel(execution)));
    }

    public void endStage(UUID executionId, JobStage stage, Instant end) {
        String endTimeDateFormatted = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(end));
        logger.debug("Ending stage {} for job execution {} at {}", stage, executionId, endTimeDateFormatted);
        Optional<ExecutingJob> executingJob = Optional.ofNullable(executingJobMap.getOrDefault(executionId, null));
        executingJob
            .flatMap(job -> job.getStage(stage))
            .ifPresent(jobStage -> jobStage.endStage(DateUtils.fromInstantUTC(end).toInstant()));
        executingJob.ifPresent(execution -> jobCompletionStatusAccessor.saveExecutionStatus(createStatusModel(execution)));
    }

    public void purgeJob(UUID executionId) {
        logger.debug("Purging job execution {}", executionId);
        executingJobMap.remove(executionId);
    }

    public void incrementRemainingEvents(UUID jobExecutionId, int eventCount) {
        logger.debug("Incrementing event count for job execution {} by {}", jobExecutionId, eventCount);
        getExecutingJob(jobExecutionId)
            .ifPresent(executingJob -> executingJob.incrementRemainingEventCount(eventCount));
    }

    public void decrementRemainingEvents(UUID jobExecutionId) {
        logger.debug("Decrementing event count for job execution {}", jobExecutionId);
        getExecutingJob(jobExecutionId)
            .ifPresent(ExecutingJob::decrementRemainingEventCount);
    }

    public boolean hasRemainingEvents(UUID jobExecutionId) {
        return getExecutingJob(jobExecutionId)
            .map(ExecutingJob::getRemainingEvents)
            .stream().anyMatch(remainingEventCount -> remainingEventCount > 0);
    }

    public boolean hasSentExpectedNotifications(UUID jobExecutionId) {
        return getExecutingJob(jobExecutionId)
            .stream()
            .anyMatch(executingJob -> executingJob.getExpectedNotificationsToSend() == executingJob.getNotificationsSent());
    }

    public void incrementExpectedNotificationsSent(UUID jobExecutionId, int notificationCount) {
        logger.debug("Incrementing sent notification count for job execution {} by {}", jobExecutionId, notificationCount);
        Optional<ExecutingJob> executingJob = getExecutingJob(jobExecutionId);
        executingJob.ifPresent(execution -> execution.incrementExpectedNotificationsSent(notificationCount));
    }

    public void incrementSentNotificationCount(UUID jobExecutionId, int notificationCount) {
        logger.debug("Incrementing sent notification count for job execution {} by {}", jobExecutionId, notificationCount);
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

    private JobCompletionStatusModel createEmptyStatusModel(UUID jobConfigId) {
        OffsetDateTime startTime = DateUtils.createCurrentDateTimestamp();
        return new JobCompletionStatusModel(
            jobConfigId,
            0L,
            0L,
            0L,
            0L,
            AuditEntryStatus.PENDING.name(),
            startTime,
            startTime,
            JobCompletionStatusDurations.empty()
        );
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

    private JobCompletionStatusModel createStatusModel(ExecutingJob executingJob) {
        AuditEntryStatus jobStatus = executingJob.getStatus();
        UUID jobConfigId = executingJob.getJobConfigId();
        long successCount = AuditEntryStatus.SUCCESS == jobStatus ? 1L : 0L;
        long failureCount = AuditEntryStatus.FAILURE == jobStatus ? 1L : 0L;
        OffsetDateTime startTime = DateUtils.fromInstantUTC(executingJob.getStart());
        OffsetDateTime endTime = executingJob.getEnd().map(DateUtils::fromInstantUTC).orElse(DateUtils.createCurrentDateTimestamp());

        JobCompletionStatusDurations durations = new JobCompletionStatusDurations(
            calculateNanosecondDuration(executingJob.getStart(), executingJob.getEnd().orElse(Instant.now())),
            calculateJobStageDuration(executingJob, JobStage.NOTIFICATION_PROCESSING),
            calculateJobStageDuration(executingJob, JobStage.CHANNEL_PROCESSING),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_CREATION),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_COMMENTING),
            calculateJobStageDuration(executingJob, JobStage.ISSUE_TRANSITION)
        );

        return new JobCompletionStatusModel(
            jobConfigId,
            executingJob.getNotificationsSent(),
            executingJob.getTotalNotificationCount(),
            successCount,
            failureCount,
            jobStatus.name(),
            endTime,
            startTime,
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
