/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEventListener;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.google.gson.Gson;

@Component
public class AzureBoardsCreateIssueEventListener extends IssueTrackerCreateIssueEventListener {
    @Autowired
    public AzureBoardsCreateIssueEventListener(
        Gson gson,
        AzureBoardsChannelKey channelKey,
        AzureBoardsCreateIssueEventHandler eventHandler
    ) {
        super(gson, channelKey, eventHandler);
    }
}
