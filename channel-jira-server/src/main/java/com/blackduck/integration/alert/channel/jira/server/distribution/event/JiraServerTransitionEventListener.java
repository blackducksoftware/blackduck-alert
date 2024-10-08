/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;

@Component
public class JiraServerTransitionEventListener extends IssueTrackerTransitionEventListener<JiraServerTransitionEvent> {
    @Autowired
    public JiraServerTransitionEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JiraServerChannelKey channelKey,
        JiraServerTransitionEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, JiraServerTransitionEvent.class, eventHandler);
    }
}
