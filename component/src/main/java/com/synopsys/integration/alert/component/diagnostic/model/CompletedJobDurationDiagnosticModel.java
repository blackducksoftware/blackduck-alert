package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class CompletedJobDurationDiagnosticModel extends AlertSerializableModel {

    private static final long serialVersionUID = -7963260099077205918L;

    private final String jobDuration;
    private final String notificationProcessing;
    private final String channelProcessing;
    private final String issueCreation;
    private final String issueCommenting;
    private final String issueTransition;

    public CompletedJobDurationDiagnosticModel(
        String jobDuration,
        String notificationProcessing,
        String channelProcessing,
        String issueCreation,
        String issueCommenting,
        String issueTransition
    ) {
        this.jobDuration = jobDuration;
        this.notificationProcessing = notificationProcessing;
        this.channelProcessing = channelProcessing;
        this.issueCreation = issueCreation;
        this.issueCommenting = issueCommenting;
        this.issueTransition = issueTransition;
    }

    public String getJobDuration() {
        return jobDuration;
    }

    public String getNotificationProcessing() {
        return notificationProcessing;
    }

    public String getChannelProcessing() {
        return channelProcessing;
    }

    public String getIssueCreation() {
        return issueCreation;
    }

    public String getIssueCommenting() {
        return issueCommenting;
    }

    public String getIssueTransition() {
        return issueTransition;
    }
}
