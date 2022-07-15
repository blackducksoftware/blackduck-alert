/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEventListener;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

@Component
public class AzureBoardsCreateIssueEventListener extends IssueTrackerCreateIssueEventListener {
    public AzureBoardsCreateIssueEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        AzureBoardsChannelKey channelKey,
        AzureBoardsCreateIssueEventHandler eventHandler
    ) {
        super(gson, taskExecutor, channelKey, eventHandler);
    }
}
