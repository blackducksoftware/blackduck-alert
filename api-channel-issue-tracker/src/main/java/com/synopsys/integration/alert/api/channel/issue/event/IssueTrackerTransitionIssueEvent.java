package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.event.AlertEvent;

public class IssueTrackerTransitionIssueEvent<T extends Serializable> extends AlertEvent {
    private static final long serialVersionUID = 2225961487898754563L;
    private final UUID jobId;
    private IssueTransitionModel<T> transitionModel;

    public IssueTrackerTransitionIssueEvent(String destination, UUID jobId, IssueTransitionModel<T> transitionModel) {
        super(destination);
        this.jobId = jobId;
        this.transitionModel = transitionModel;
    }

    public UUID getJobId() {
        return jobId;
    }

    public IssueTransitionModel<T> getTransitionModel() {
        return transitionModel;
    }
}
