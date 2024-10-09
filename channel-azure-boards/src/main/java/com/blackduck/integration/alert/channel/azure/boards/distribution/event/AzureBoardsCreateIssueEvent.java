package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;

public class AzureBoardsCreateIssueEvent extends IssueTrackerCreateIssueEvent {
    public AzureBoardsCreateIssueEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCreationModel creationModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds, creationModel);
    }
}
