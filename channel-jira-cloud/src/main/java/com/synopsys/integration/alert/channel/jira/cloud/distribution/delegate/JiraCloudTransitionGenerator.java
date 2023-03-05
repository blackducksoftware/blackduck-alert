/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.event.JiraCloudTransitionEvent;
import com.synopsys.integration.alert.api.descriptor.JiraCloudChannelKey;

public class JiraCloudTransitionGenerator implements IssueTrackerTransitionEventGenerator<String> {
    private JiraCloudChannelKey channelKey;
    private UUID parentEventId;
    private final UUID jobExecutionId;
    private UUID jobId;

    private Set<Long> notificationIds;

    public JiraCloudTransitionGenerator(JiraCloudChannelKey channelKey, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<String> generateEvent(IssueTransitionModel<String> model) {
        return new JiraCloudTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
