/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCommentEventGenerator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.AzureBoardsCommentEvent;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

public class AzureBoardsCommentGenerator implements IssueTrackerCommentEventGenerator<Integer> {

    private AzureBoardsChannelKey channelKey;
    private UUID jobId;

    public AzureBoardsCommentGenerator(AzureBoardsChannelKey channelKey, UUID jobId) {
        this.channelKey = channelKey;
        this.jobId = jobId;
    }

    @Override
    public IssueTrackerCommentEvent<Integer> generateEvent(IssueCommentModel<Integer> model) {
        return new AzureBoardsCommentEvent(IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), jobId, model);
    }
}
