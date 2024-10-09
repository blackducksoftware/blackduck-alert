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

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCommentEventGenerator;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.distribution.event.JiraServerCommentEvent;

public class JiraServerCommentGenerator implements IssueTrackerCommentEventGenerator<String> {
    private final JiraServerChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public JiraServerCommentGenerator(JiraServerChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCommentEvent<String> generateEvent(IssueCommentModel<String> model) {
        return new JiraServerCommentEvent(IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), jobExecutionId, jobId, notificationIds, model);
    }
}
