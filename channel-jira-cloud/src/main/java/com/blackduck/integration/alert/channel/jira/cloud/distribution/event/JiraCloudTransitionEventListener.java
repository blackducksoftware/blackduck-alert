package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.google.gson.Gson;

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
