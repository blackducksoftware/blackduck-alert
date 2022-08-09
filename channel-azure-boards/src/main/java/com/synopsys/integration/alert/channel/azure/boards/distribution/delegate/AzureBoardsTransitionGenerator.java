/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.AzureBoardsTransitionEvent;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

public class AzureBoardsTransitionGenerator implements IssueTrackerTransitionEventGenerator<Integer> {
    private AzureBoardsChannelKey channelKey;
    private UUID jobId;

    public AzureBoardsTransitionGenerator(AzureBoardsChannelKey channelKey, UUID jobId) {
        this.channelKey = channelKey;
        this.jobId = jobId;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<Integer> generateEvent(IssueTransitionModel<Integer> model) {
        return new AzureBoardsTransitionEvent(IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey), jobId, model);
    }
}
