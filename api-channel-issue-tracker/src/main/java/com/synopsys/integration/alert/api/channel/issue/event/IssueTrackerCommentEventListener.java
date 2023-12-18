/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.distribution.JobSubTaskMessageListener;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKey;

public abstract class IssueTrackerCommentEventListener<T extends JobSubTaskEvent> extends JobSubTaskMessageListener<T> {
    protected IssueTrackerCommentEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        ChannelKey channelKey,
        Class<T> eventClass,
        IssueTrackerCommentEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), eventClass, eventHandler);
    }
}
