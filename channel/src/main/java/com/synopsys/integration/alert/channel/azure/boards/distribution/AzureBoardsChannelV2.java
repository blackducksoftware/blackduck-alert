/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.IssueTrackerChannel;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsChannelV2 extends IssueTrackerChannel<AzureBoardsJobDetailsModel, Integer> {
    @Autowired
    public AzureBoardsChannelV2(AzureBoardsProcessorFactory processorFactory, IssueTrackerResponsePostProcessor responsePostProcessor) {
        super(processorFactory, responsePostProcessor);
    }

}
