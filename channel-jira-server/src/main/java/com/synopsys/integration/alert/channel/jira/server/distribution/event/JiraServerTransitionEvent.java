/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;

public class JiraServerTransitionEvent extends IssueTrackerTransitionIssueEvent<String> {
    private static final long serialVersionUID = -4019105794105848692L;

    public JiraServerTransitionEvent(
        String destination,
        UUID parentEventId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<String> transitionModel
    ) {
        super(destination, parentEventId, jobId, notificationIds, transitionModel);
    }
}
