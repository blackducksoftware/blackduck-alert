package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

public class IssueTrackerTransitionIssueEvent<T extends Serializable> extends JobSubTaskEvent {
    private static final long serialVersionUID = 2225961487898754563L;

    public static String createDefaultEventDestination(ChannelKey channelKey) {
        return String.format("%s_issue_transition", channelKey.getUniversalKey());
    }

    private IssueTransitionModel<T> transitionModel;

    public IssueTrackerTransitionIssueEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueTransitionModel<T> transitionModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds);
        this.transitionModel = transitionModel;
    }

    public IssueTransitionModel<T> getTransitionModel() {
        return transitionModel;
    }
}
