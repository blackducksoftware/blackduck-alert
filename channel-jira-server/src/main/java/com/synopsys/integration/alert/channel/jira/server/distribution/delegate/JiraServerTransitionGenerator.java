/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.JiraServerTransitionEvent;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

public class JiraServerTransitionGenerator implements IssueTrackerTransitionEventGenerator<String> {
    private final JiraServerChannelKey channelKey;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public JiraServerTransitionGenerator(JiraServerChannelKey channelKey, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<String> generateEvent(IssueTransitionModel<String> model) {
        return new JiraServerTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
