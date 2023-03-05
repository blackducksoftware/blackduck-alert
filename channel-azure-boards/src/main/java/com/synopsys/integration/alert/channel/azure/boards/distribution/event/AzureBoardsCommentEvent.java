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

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;

public class AzureBoardsCommentEvent extends IssueTrackerCommentEvent<Integer> {

    private static final long serialVersionUID = 6009574433460787684L;

    public AzureBoardsCommentEvent(
        String destination,
        UUID parentEventId,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCommentModel<Integer> commentModel
    ) {
        super(destination, parentEventId, jobExecutionId, jobId, notificationIds, commentModel);
    }
}
