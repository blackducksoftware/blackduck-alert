/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionEventListener;
import com.synopsys.integration.alert.api.descriptor.JiraCloudChannelKey;

@Component
public class JiraCloudTransitionEventListener extends IssueTrackerTransitionEventListener<JiraCloudTransitionEvent> {
    @Autowired
    public JiraCloudTransitionEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JiraCloudChannelKey channelKey,
        JiraCloudTransitionEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, JiraCloudTransitionEvent.class, eventHandler);
    }
}
