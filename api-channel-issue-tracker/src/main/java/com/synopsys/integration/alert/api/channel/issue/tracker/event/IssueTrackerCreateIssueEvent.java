/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.event;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class IssueTrackerCreateIssueEvent extends JobSubTaskEvent {
    private static final long serialVersionUID = 9165621968176192549L;

    public static String createDefaultEventDestination(ChannelKey channelKey) {
        return String.format("%s_issue_create", channelKey.getUniversalKey());
    }

    private IssueCreationModel creationModel;

    public IssueTrackerCreateIssueEvent(
        String destination,
        UUID parentEventId,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, parentEventId, jobExecutionId, jobId, notificationIds);
        this.creationModel = creationModel;
    }

    public IssueCreationModel getCreationModel() {
        return creationModel;
    }
}
