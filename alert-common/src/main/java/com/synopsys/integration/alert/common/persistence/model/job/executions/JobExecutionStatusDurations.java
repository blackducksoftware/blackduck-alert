package com.synopsys.integration.alert.common.persistence.model.job.executions;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class JobExecutionStatusDurations extends AlertSerializableModel {
    private static final long serialVersionUID = -5127766336418643920L;
    private final Long jobDuration;
    private final Long notificationProcessingDuration;
    private final Long channelProcessingDuration;
    private final Long issueCreationDuration;
    private final Long issueCommentingDuration;
    private final Long issueTransitionDuration;

    public static JobExecutionStatusDurations empty() {
        return new JobExecutionStatusDurations(0L, 0L, 0L, 0L, 0L, 0L);
    }

    public JobExecutionStatusDurations(
        Long jobDuration,
        Long notificationProcessingDuration,
        Long channelProcessingDuration,
        Long issueCreationDuration,
        Long issueCommentingDuration,
        Long issueTransitionDuration
    ) {
        this.jobDuration = jobDuration;
        this.notificationProcessingDuration = notificationProcessingDuration;
        this.channelProcessingDuration = channelProcessingDuration;
        this.issueCreationDuration = issueCreationDuration;
        this.issueCommentingDuration = issueCommentingDuration;
        this.issueTransitionDuration = issueTransitionDuration;
    }

    public Long getJobDuration() {
        return jobDuration;
    }

    public Optional<Long> getNotificationProcessingDuration() {
        return Optional.ofNullable(notificationProcessingDuration);
    }

    public Optional<Long> getChannelProcessingDuration() {
        return Optional.ofNullable(channelProcessingDuration);
    }

    public Optional<Long> getIssueCreationDuration() {
        return Optional.ofNullable(issueCreationDuration);
    }

    public Optional<Long> getIssueCommentingDuration() {
        return Optional.ofNullable(issueCommentingDuration);
    }

    public Optional<Long> getIssueTransitionDuration() {
        return Optional.ofNullable(issueTransitionDuration);
    }
}
