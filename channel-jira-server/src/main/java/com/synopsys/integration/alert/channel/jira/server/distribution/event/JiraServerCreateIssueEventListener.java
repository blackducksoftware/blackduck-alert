/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEventHandler;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEventListener;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

@Component
public class JiraServerCreateIssueEventListener extends IssueTrackerCreateIssueEventListener {
    public JiraServerCreateIssueEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JiraServerChannelKey channelKey,
        IssueTrackerCreateIssueEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, eventHandler);
    }
}
