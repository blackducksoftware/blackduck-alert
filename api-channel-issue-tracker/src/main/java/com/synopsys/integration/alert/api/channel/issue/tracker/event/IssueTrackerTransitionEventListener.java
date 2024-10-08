/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.event;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.distribution.JobSubTaskMessageListener;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

public abstract class IssueTrackerTransitionEventListener<T extends JobSubTaskEvent> extends JobSubTaskMessageListener<T> {
    protected IssueTrackerTransitionEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        ChannelKey channelKey,
        Class<T> eventClass,
        IssueTrackerTransitionEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey), eventClass, eventHandler);
    }
}
