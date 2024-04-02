/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.event;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKey;

public class IssueTrackerCommentEvent<T extends Serializable> extends JobSubTaskEvent {
    private static final long serialVersionUID = 867746168597376739L;

    public static String createDefaultEventDestination(ChannelKey channelKey) {
        return String.format("%s_issue_comment", channelKey.getUniversalKey());
    }

    private IssueCommentModel<T> commentModel;

    public IssueTrackerCommentEvent(
        String destination,
        UUID jobExecutionId,
        UUID jobId,
        Set<Long> notificationIds,
        IssueCommentModel<T> commentModel
    ) {
        super(destination, jobExecutionId, jobId, notificationIds);
        this.commentModel = commentModel;
    }

    public IssueCommentModel<T> getCommentModel() {
        return commentModel;
    }
}
