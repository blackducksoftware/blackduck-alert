package com.blackduck.integration.alert.common.channel.issuetracker;

import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.google.gson.Gson;

public abstract class IssueTrackerCallbackEventListener extends AlertMessageListener<IssueTrackerCallbackEvent> {
    public static final String ISSUE_TRACKER_CALLBACK_DESTINATION_NAME = IssueTrackerCallbackEventListener.class.getSimpleName();

    protected IssueTrackerCallbackEventListener(Gson gson, TaskExecutor taskExecutor, AlertEventHandler<IssueTrackerCallbackEvent> eventHandler) {
        super(gson, taskExecutor, ISSUE_TRACKER_CALLBACK_DESTINATION_NAME, IssueTrackerCallbackEvent.class, eventHandler);
    }

}
