/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

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
