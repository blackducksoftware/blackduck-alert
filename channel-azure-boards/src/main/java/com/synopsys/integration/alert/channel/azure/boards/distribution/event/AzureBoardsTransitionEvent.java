/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;

public class AzureBoardsTransitionEvent extends IssueTrackerTransitionIssueEvent<Integer> {
    public AzureBoardsTransitionEvent(
        String destination,
        UUID parentEventId,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<Integer> transitionModel
    ) {
        super(destination, parentEventId, jobExecutionId, jobId, notificationIds, transitionModel);
    }
}
