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

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.event.JiraCloudCreateIssueEvent;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

public class JiraCloudCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final JiraCloudChannelKey channelKey;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final UUID jobId;

    private final Set<Long> notificationIds;

    public JiraCloudCreateEventGenerator(JiraCloudChannelKey channelKey, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new JiraCloudCreateIssueEvent(IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), parentEventId, jobExecutionId, jobId, notificationIds, model);
    }
}
