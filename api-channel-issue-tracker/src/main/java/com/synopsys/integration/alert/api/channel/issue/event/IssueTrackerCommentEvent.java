/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class IssueTrackerCommentEvent<T extends Serializable> extends AlertEvent {
    private static final long serialVersionUID = 867746168597376739L;

    public static String createDefaultEventDestination(ChannelKey channelKey) {
        return String.format("%s_issue_comment", channelKey.getUniversalKey());
    }
    
    private final UUID jobId;
    private IssueCommentModel<T> commentModel;

    public IssueTrackerCommentEvent(String destination, UUID jobId, IssueCommentModel<T> commentModel) {
        super(destination);
        this.jobId = jobId;
        this.commentModel = commentModel;
    }

    public UUID getJobId() {
        return jobId;
    }

    public IssueCommentModel<T> getCommentModel() {
        return commentModel;
    }
}
