/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.channel.azure.boards.distribution.event.AzureBoardsTransitionEvent;

public class AzureBoardsTransitionGenerator implements IssueTrackerTransitionEventGenerator<Integer> {
    private final AzureBoardsChannelKey channelKey;
    private final UUID jobExecutionId;
    private final UUID jobId;

    private final Set<Long> notificationIds;

    public AzureBoardsTransitionGenerator(AzureBoardsChannelKey channelKey, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        this.channelKey = channelKey;
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    @Override
    public IssueTrackerTransitionIssueEvent<Integer> generateEvent(IssueTransitionModel<Integer> model) {
        return new AzureBoardsTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
    }
}
