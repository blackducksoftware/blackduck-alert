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

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;

public class AzureBoardsCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    public AzureBoardsCreateIssueEvent(
        String destination,
        UUID parentEventId,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, parentEventId, jobExecutionId, jobId, notificationIds, creationModel);
    }
}
