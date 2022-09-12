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
    private AzureBoardsChannelKey channelKey;
    private UUID parentEventId;
    private UUID jobId;
    private Set<Long> notificationIds;

    public AzureBoardsCreateEventGenerator(AzureBoardsChannelKey channelKey, UUID parentEventId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model) {
        return new AzureBoardsCreateIssueEvent(IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), parentEventId, jobId, notificationIds, model);
    }
}
