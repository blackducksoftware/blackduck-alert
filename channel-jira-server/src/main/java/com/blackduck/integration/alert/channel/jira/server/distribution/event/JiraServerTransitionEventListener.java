/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.google.gson.Gson;

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
