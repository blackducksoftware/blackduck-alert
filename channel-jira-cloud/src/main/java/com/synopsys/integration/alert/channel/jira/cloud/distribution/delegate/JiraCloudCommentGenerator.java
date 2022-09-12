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
    private JiraCloudChannelKey channelKey;
    private UUID parentEventId;
    private UUID jobId;

    private Set<Long> notificationIds;

    public JiraCloudCommentGenerator(JiraCloudChannelKey channelKey, UUID parentEventId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCommentEvent<String> generateEvent(IssueCommentModel<String> model) {
        return new JiraCloudCommentEvent(IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), parentEventId, jobId, notificationIds, model);
    }
}
