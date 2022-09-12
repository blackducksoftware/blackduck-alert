/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;

public class JiraCloudCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    private static final long serialVersionUID = -8590565054074040050L;

    public JiraCloudCreateIssueEvent(
        String destination,
        UUID parentEventId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, parentEventId, jobId, notificationIds, creationModel);
    }
}
