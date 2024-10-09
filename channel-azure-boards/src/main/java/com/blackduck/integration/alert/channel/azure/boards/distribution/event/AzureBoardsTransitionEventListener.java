/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionEventListener;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.google.gson.Gson;

@Component
public class AzureBoardsTransitionEventListener extends IssueTrackerTransitionEventListener<AzureBoardsTransitionEvent> {
    @Autowired
    public AzureBoardsTransitionEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        AzureBoardsChannelKey channelKey,
        AzureBoardsTransitionEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, AzureBoardsTransitionEvent.class, eventHandler);
    }
}
