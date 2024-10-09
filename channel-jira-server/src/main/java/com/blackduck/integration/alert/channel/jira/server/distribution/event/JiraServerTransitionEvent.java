package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;

public class JiraServerTransitionEvent extends IssueTrackerTransitionIssueEvent<String> {
    private static final long serialVersionUID = -4019105794105848692L;

    public JiraServerTransitionEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<String> transitionModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, transitionModel);
    }
}
