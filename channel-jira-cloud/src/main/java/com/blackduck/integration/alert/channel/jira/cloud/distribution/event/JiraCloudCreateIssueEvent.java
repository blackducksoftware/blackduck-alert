/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;

public class JiraCloudCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    private static final long serialVersionUID = -8590565054074040050L;

    public JiraCloudCreateIssueEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, creationModel);
    }
}
