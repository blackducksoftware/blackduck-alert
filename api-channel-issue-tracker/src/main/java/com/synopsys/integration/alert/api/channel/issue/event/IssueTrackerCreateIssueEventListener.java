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
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public abstract class IssueTrackerCreateIssueEventListener extends JobSubTaskMessageListener<IssueTrackerCreateIssueEvent> {
    protected IssueTrackerCreateIssueEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        ChannelKey channelKey,
        IssueTrackerCreateIssueEventHandler eventHandler
    ) {
        super(gson, taskExecutor, IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), IssueTrackerCreateIssueEvent.class, eventHandler);
    }
}
