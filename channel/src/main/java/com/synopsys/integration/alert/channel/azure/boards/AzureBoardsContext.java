/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;

public class AzureBoardsContext extends IssueTrackerContext {
    public AzureBoardsContext(AzureBoardsProperties serviceConfig, IssueConfig issueConfig) {
        super(serviceConfig, issueConfig);
    }

    @Override
    public AzureBoardsProperties getIssueTrackerConfig() {
        return (AzureBoardsProperties) super.getIssueTrackerConfig();
    }

}
