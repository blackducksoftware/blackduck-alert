/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.action.IssueTrackerTestAction;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.channel.azure.boards.distribution.AzureBoardsMessageSenderFactory;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsDistributionTestAction extends IssueTrackerTestAction<AzureBoardsJobDetailsModel, Integer> {
    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannelKey channelKey, AzureBoardsMessageSenderFactory messageSenderFactory) {
        super(channelKey, messageSenderFactory);
    }

    @Override
    protected boolean hasResolveTransition(AzureBoardsJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getWorkItemCompletedState());
    }

    @Override
    protected boolean hasReopenTransition(AzureBoardsJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getWorkItemReopenState());
    }

}
