package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;

public class AzureBoardsTransitionEvent extends IssueTrackerTransitionIssueEvent<Integer> {
    public AzureBoardsTransitionEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<Integer> transitionModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, transitionModel);
    }
}
