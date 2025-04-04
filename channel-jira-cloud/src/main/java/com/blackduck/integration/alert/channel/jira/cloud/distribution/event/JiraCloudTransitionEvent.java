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

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;

public class JiraCloudTransitionEvent extends IssueTrackerTransitionIssueEvent<String> {
    private static final long serialVersionUID = -5217352371581221553L;

    public JiraCloudTransitionEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<String> transitionModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, transitionModel);
    }
}
