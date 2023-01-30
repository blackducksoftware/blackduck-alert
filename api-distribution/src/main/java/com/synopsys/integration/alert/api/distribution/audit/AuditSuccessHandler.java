package com.synopsys.integration.alert.api.distribution.audit;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class AuditSuccessHandler implements AlertEventHandler<AuditSuccessEvent> {
    private final ExecutingJobManager executingJobManager;
    private final JobExecutionStatusAccessor jobExecutionStatusAccessor;

    public AuditSuccessHandler(ExecutingJobManager executingJobManager, JobExecutionStatusAccessor jobExecutionStatusAccessor) {
        this.executingJobManager = executingJobManager;
        this.jobExecutionStatusAccessor = jobExecutionStatusAccessor;
    }

    @Override
    public void handle(AuditSuccessEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        executingJobManager.getExecutingJob(jobExecutionId)
            .filter(Predicate.not(ExecutingJob::hasRemainingEvents))
            .ifPresent(executingJob -> {
                if (executingJob.isCompleted()) {
                    executingJobManager.endJobWithSuccess(jobExecutionId, event.getCreatedTimestamp().toInstant());
                    jobExecutionStatusAccessor.saveExecutionStatus(createStatusModel(executingJob));
                }
                //executingJobManager.purgeJob(jobExecutionId);
            });
    }

    private JobExecutionStatusModel createStatusModel(ExecutingJob executingJob) {
        UUID jobConfigId = executingJob.getJobConfigId();
        JobExecutionStatusModel resultStatus;
        Optional<JobExecutionStatusModel> status = jobExecutionStatusAccessor.getJobExecutionStatus(jobConfigId);
        if (status.isPresent()) {
            JobExecutionStatusModel currentStatus = status.get();
            JobExecutionStatusDurations currentDurations = currentStatus.getDurations();
            Long jobDuration = calculateNanosecondDuration(executingJob.getStart(), executingJob.getEnd().orElse(Instant.now()));
            Long processingStageDuration = calculateJobStageDuration(executingJob, JobStage.NOTIFICATION_PROCESSING);
            Long channelProcessingStageDuration = calculateJobStageDuration(executingJob, JobStage.CHANNEL_PROCESSING);
            Long issueCreationDuration = calculateJobStageDuration(executingJob, JobStage.ISSUE_CREATION);
            Long issueCommentingDuration = calculateJobStageDuration(executingJob, JobStage.ISSUE_COMMENTING);
            Long issueResolvingDuration = calculateJobStageDuration(executingJob, JobStage.ISSUE_RESOLVING);

            JobExecutionStatusDurations durations = new JobExecutionStatusDurations(
                calculateAverage(currentDurations.getJobDurationMillisec(), jobDuration),
                calculateAverage(currentDurations.getNotificationProcessingDuration().orElse(0L), processingStageDuration),
                calculateAverage(currentDurations.getChannelProcessingDuration().orElse(0L), channelProcessingStageDuration),
                calculateAverage(currentDurations.getIssueCreationDuration().orElse(0L), issueCreationDuration),
                calculateAverage(currentDurations.getIssueCommentingDuration().orElse(0L), issueCommentingDuration),
                calculateAverage(currentDurations.getIssueTransitionDuration().orElse(0L), issueResolvingDuration)
            );

            resultStatus = new JobExecutionStatusModel(
                executingJob.getJobConfigId(),
                Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue(),
                Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue() + currentStatus.getTotalNotificationCount(),
                currentStatus.getSuccessCount() + 1L,
                currentStatus.getFailureCount(),
                AuditEntryStatus.SUCCESS.name(),
                DateUtils.fromInstantUTC(executingJob.getEnd().orElse(Instant.now())),
                durations
            );

        } else {
            JobExecutionStatusDurations durations = new JobExecutionStatusDurations(
                calculateNanosecondDuration(executingJob.getStart(), executingJob.getEnd().orElse(Instant.now())),
                calculateJobStageDuration(executingJob, JobStage.NOTIFICATION_PROCESSING),
                calculateJobStageDuration(executingJob, JobStage.CHANNEL_PROCESSING),
                calculateJobStageDuration(executingJob, JobStage.ISSUE_CREATION),
                calculateJobStageDuration(executingJob, JobStage.ISSUE_COMMENTING),
                calculateJobStageDuration(executingJob, JobStage.ISSUE_RESOLVING)
            );
            resultStatus = new JobExecutionStatusModel(
                executingJob.getJobConfigId(),
                Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue(),
                Integer.valueOf(executingJob.getProcessedNotificationCount()).longValue(),
                1L,
                0L,
                AuditEntryStatus.SUCCESS.name(),
                DateUtils.fromInstantUTC(executingJob.getEnd().orElse(Instant.now())),
                durations
            );
        }

        return resultStatus;
    }

    private Long calculateAverage(Long firstValue, Long secondValue) {
        if (firstValue < 1 || secondValue < 1) {
            return 0L;
        }
        return (firstValue + secondValue) / 2;
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
