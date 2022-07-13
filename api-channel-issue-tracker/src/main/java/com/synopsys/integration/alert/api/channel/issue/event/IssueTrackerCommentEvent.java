package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.event.AlertEvent;

public class IssueTrackerCommentEvent<T extends Serializable> extends AlertEvent {
    private static final long serialVersionUID = 867746168597376739L;
    private final UUID jobId;
    private IssueCommentModel<T> commentModel;

    public IssueTrackerCommentEvent(String destination, UUID jobId, IssueCommentModel<T> commentModel) {
        super(destination);
        this.jobId = jobId;
        this.commentModel = commentModel;
    }

    public IssueCommentModel<T> getCommentModel() {
        return commentModel;
    }
}
