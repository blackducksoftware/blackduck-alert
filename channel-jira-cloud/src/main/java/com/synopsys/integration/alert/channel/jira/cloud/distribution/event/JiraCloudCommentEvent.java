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

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;

public class JiraCloudCommentEvent extends IssueTrackerCommentEvent<String> {

    private static final long serialVersionUID = 6009574433460787684L;

    public JiraCloudCommentEvent(
        String destination,
        UUID parentEventId,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCommentModel<String> commentModel
    ) {
        super(destination, parentEventId, jobExecutionId, jobId, notificationIds, commentModel);
    }
}
