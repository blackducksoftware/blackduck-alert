/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.AzureBoardsCreateIssueEvent;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

public class AzureBoardsCreateEventGenerator implements IssueTrackerCreationEventGenerator {
    private final AzureBoardsChannelKey channelKey;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public AzureBoardsCreateEventGenerator(AzureBoardsChannelKey channelKey, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new AzureBoardsCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
