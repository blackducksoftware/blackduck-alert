/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.event.JiraCloudCreateIssueEvent;

public class JiraCloudCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final JiraCloudChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;

    private final Set<Long> notificationIds;

    public JiraCloudCreateEventGenerator(JiraCloudChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new JiraCloudCreateIssueEvent(IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), jobExecutionId, jobId, notificationIds, model);
    }
}
