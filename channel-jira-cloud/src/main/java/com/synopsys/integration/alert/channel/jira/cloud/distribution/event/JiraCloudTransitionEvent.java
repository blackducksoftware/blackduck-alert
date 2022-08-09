/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;

public class JiraCloudTransitionEvent extends IssueTrackerTransitionIssueEvent<String> {
    public JiraCloudTransitionEvent(String destination, UUID jobId, IssueTransitionModel<String> transitionModel) {
        super(destination, jobId, transitionModel);
    }
}
