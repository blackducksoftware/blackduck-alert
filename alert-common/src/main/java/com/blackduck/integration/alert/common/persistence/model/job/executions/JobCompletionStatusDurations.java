/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.executions;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JobCompletionStatusDurations extends AlertSerializableModel {
    private static final long serialVersionUID = -5127766336418643920L;
    private final long jobDuration;
    private final long notificationProcessingDuration;
    private final long channelProcessingDuration;
    private final long issueCreationDuration;
    private final long issueCommentingDuration;
    private final long issueTransitionDuration;

    public static JobCompletionStatusDurations empty() {
        return new JobCompletionStatusDurations(0L, 0L, 0L, 0L, 0L, 0L);
    }

    public JobCompletionStatusDurations(
        long jobDuration,
        long notificationProcessingDuration,
        long channelProcessingDuration,
        long issueCreationDuration,
        long issueCommentingDuration,
        long issueTransitionDuration
    ) {
        this.jobDuration = jobDuration;
        this.notificationProcessingDuration = notificationProcessingDuration;
        this.channelProcessingDuration = channelProcessingDuration;
        this.issueCreationDuration = issueCreationDuration;
        this.issueCommentingDuration = issueCommentingDuration;
        this.issueTransitionDuration = issueTransitionDuration;
    }

    public long getJobDuration() {
        return jobDuration;
    }

    public long getNotificationProcessingDuration() {
        return notificationProcessingDuration;
    }

    public long getChannelProcessingDuration() {
        return channelProcessingDuration;
    }

    public long getIssueCreationDuration() {
        return issueCreationDuration;
    }

    public long getIssueCommentingDuration() {
        return issueCommentingDuration;
    }

    public long getIssueTransitionDuration() {
        return issueTransitionDuration;
    }
}
