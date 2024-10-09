package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;

public class JiraCloudCommentEvent extends IssueTrackerCommentEvent<String> {

    private static final long serialVersionUID = 6009574433460787684L;

    public JiraCloudCommentEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCommentModel<String> commentModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, commentModel);
    }
}
