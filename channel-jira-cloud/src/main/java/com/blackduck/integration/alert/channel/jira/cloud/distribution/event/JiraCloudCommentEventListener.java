/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;

@Component
public class JiraCloudCommentEventListener extends IssueTrackerCommentEventListener<JiraCloudCommentEvent> {
    @Autowired
    public JiraCloudCommentEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JiraCloudChannelKey channelKey,
        JiraCloudCommentEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, JiraCloudCommentEvent.class, eventHandler);
    }
}
