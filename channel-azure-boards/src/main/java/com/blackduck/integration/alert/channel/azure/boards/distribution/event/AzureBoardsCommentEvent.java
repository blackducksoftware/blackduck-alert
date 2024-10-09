package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;

public class AzureBoardsCommentEvent extends IssueTrackerCommentEvent<Integer> {

    private static final long serialVersionUID = 6009574433460787684L;

    public AzureBoardsCommentEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCommentModel<Integer> commentModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, commentModel);
    }
}
