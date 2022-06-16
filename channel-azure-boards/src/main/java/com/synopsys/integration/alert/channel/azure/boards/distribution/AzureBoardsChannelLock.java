/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.IssueTrackerChannelLock;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

@Component
public class AzureBoardsChannelLock extends IssueTrackerChannelLock {
    @Autowired
    public AzureBoardsChannelLock(AzureBoardsChannelKey channelKey) {
        super(channelKey.getUniversalKey());
    }
}
