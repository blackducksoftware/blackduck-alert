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

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCommentEventGenerator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.event.JiraCloudCommentEvent;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

public class JiraCloudCommentGenerator implements IssueTrackerCommentEventGenerator<String> {
    private final JiraCloudChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;

    private final Set<Long> notificationIds;

    public JiraCloudCommentGenerator(JiraCloudChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCommentEvent<String> generateEvent(IssueCommentModel<String> model) {
        return new JiraCloudCommentEvent(IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), jobExecutionId, jobId, notificationIds, model);
    }
}
