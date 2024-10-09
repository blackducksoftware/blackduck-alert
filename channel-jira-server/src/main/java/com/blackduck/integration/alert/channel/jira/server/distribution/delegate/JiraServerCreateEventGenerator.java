/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.channel.jira.server.distribution.event.JiraServerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;

public class JiraServerCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final JiraServerChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private Set<Long> notificationIds;

    public JiraServerCreateEventGenerator(JiraServerChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new JiraServerCreateIssueEvent(IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), jobExecutionId, jobId, notificationIds, model);
    }
}
