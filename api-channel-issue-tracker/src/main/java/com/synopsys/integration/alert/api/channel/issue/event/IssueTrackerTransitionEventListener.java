/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import java.io.Serializable;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public abstract class IssueTrackerTransitionEventListener<T extends Serializable> extends AlertMessageListener<IssueTrackerTransitionIssueEvent<T>> {
    protected IssueTrackerTransitionEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        ChannelKey channelKey,
        Class<IssueTrackerTransitionIssueEvent<T>> eventClass,
        IssueTrackerTransitionEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey), eventClass, eventHandler);
    }
}
