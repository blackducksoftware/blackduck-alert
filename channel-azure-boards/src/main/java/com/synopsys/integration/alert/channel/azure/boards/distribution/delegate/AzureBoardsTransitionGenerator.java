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

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.AzureBoardsTransitionEvent;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

public class AzureBoardsTransitionGenerator implements IssueTrackerTransitionEventGenerator<Integer> {
    private final AzureBoardsChannelKey channelKey;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final UUID jobId;

    private final Set<Long> notificationIds;

    public AzureBoardsTransitionGenerator(AzureBoardsChannelKey channelKey, UUID parentEventId, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<Integer> generateEvent(IssueTransitionModel<Integer> model) {
        return new AzureBoardsTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
