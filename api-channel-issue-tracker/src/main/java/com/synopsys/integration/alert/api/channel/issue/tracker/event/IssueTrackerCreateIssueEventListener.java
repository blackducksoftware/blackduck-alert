/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.event;

import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.distribution.JobSubTaskMessageListener;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

public abstract class IssueTrackerCreateIssueEventListener extends JobSubTaskMessageListener<IssueTrackerCreateIssueEvent> {
    protected IssueTrackerCreateIssueEventListener(
        Gson gson,
        ChannelKey channelKey,
        IssueTrackerCreateIssueEventHandler eventHandler
    ) {
        // Use the sync task executor to create issues in Jira synchronously.  This avoids creating duplicate issues concurrently.
        // Pull one creation event off the queue at a time and create the issue.
        super(gson, new SyncTaskExecutor(), IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey), IssueTrackerCreateIssueEvent.class, eventHandler);
    }
}
