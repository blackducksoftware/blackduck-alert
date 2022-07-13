package com.synopsys.integration.alert.api.channel.issue.event;

import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.event.AlertEvent;

public class IssueTrackerCreateIssueEvent extends AlertEvent {
    private static final long serialVersionUID = 9165621968176192549L;
    private final UUID jobId;
    private IssueCreationModel creationModel;

    public IssueTrackerCreateIssueEvent(String destination, UUID jobId, IssueCreationModel creationModel) {
        super(destination);
        this.jobId = jobId;
        this.creationModel = creationModel;
    }

    public UUID getJobId() {
        return jobId;
    }

    public IssueCreationModel getCreationModel() {
        return creationModel;
    }
}
