package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.google.gson.Gson;

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
