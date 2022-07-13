/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class IssueTrackerCreateIssueEvent extends AlertEvent {
    private static final long serialVersionUID = 9165621968176192549L;

    public static String createDefaultEventDestination(ChannelKey channelKey) {
        return String.format("%s_issue_create_queue", channelKey.getUniversalKey());
    }

    private final UUID jobId;
    private IssueCreationModel creationModel;

    public IssueTrackerCreateIssueEvent(String destination, UUID jobId, IssueCreationModel creationModel) {
        super(destination);
        this.jobId = jobId;
        this.creationModel = creationModel;
    }

    public UUID getJobId() {
        return jobId;
    }

    public IssueCreationModel getCreationModel() {
        return creationModel;
    }
}
