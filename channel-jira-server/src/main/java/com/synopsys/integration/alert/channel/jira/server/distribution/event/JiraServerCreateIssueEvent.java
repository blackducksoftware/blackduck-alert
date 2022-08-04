/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;

public class JiraServerCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    public JiraServerCreateIssueEvent(String destination, UUID jobId, IssueCreationModel creationModel) {
        super(destination, jobId, creationModel);
    }
}
