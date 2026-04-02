/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.distribution.event.JiraServerCreateIssueEvent;

public class JiraServerCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
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
        logger.debug("Generate Jira Server Create Event for Alert Issue ID {}. Job Execution ID: {}", model.getAlertIssueId(), jobExecutionId);
        return new JiraServerCreateIssueEvent(IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), jobExecutionId, jobId, notificationIds, model);
    }
}
