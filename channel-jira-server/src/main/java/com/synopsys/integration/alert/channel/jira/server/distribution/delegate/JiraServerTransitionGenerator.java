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

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.JiraServerTransitionEvent;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

public class JiraServerTransitionGenerator implements IssueTrackerTransitionEventGenerator<String> {
    private JiraServerChannelKey channelKey;
    private UUID parentEventId;
    private UUID jobId;
    private Set<Long> notificationIds;

    public JiraServerTransitionGenerator(JiraServerChannelKey channelKey, UUID parentEventId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<String> generateEvent(IssueTransitionModel<String> model) {
        return new JiraServerTransitionEvent(IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey), parentEventId, jobId, notificationIds, model);
    }
}
