/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.channel.azure.boards.distribution.event.AzureBoardsCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.api.descriptor.AzureBoardsChannelKey;

public class AzureBoardsCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final AzureBoardsChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public AzureBoardsCreateEventGenerator(AzureBoardsChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new AzureBoardsCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
