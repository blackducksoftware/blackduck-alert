package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.event.JiraCloudTransitionEvent;

public class JiraCloudTransitionGenerator implements IssueTrackerTransitionEventGenerator<String> {
    private JiraCloudChannelKey channelKey;
    private final UUID jobExecutionId;
    private UUID jobId;

    private Set<Long> notificationIds;

    public JiraCloudTransitionGenerator(JiraCloudChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<String> generateEvent(IssueTransitionModel<String> model) {
        return new JiraCloudTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
