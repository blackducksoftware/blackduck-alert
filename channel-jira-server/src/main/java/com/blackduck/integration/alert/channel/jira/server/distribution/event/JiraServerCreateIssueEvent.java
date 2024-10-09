package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;

public class JiraServerCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    private static final long serialVersionUID = -6751731646690245372L;

    public JiraServerCreateIssueEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, creationModel);
    }
}
