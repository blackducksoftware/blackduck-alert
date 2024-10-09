package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEventListener;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.google.gson.Gson;

@Component
public class JiraServerCommentEventListener extends IssueTrackerCommentEventListener<JiraServerCommentEvent> {
    @Autowired
    public JiraServerCommentEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JiraServerChannelKey channelKey,
        JiraServerCommentEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, JiraServerCommentEvent.class, eventHandler);
    }
}
