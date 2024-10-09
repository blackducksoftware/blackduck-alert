package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.distribution.JobSubTaskMessageListener;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

public abstract class IssueTrackerCommentEventListener<T extends JobSubTaskEvent> extends JobSubTaskMessageListener<T> {
    protected IssueTrackerCommentEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        ChannelKey channelKey,
        Class<T> eventClass,
        IssueTrackerCommentEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, IssueTrackerCommentEvent.createDefaultEventDestination(channelKey), eventClass, eventHandler);
    }
}
