/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.action.DistributionChannelTestAction;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsDistributionTestAction extends DistributionChannelTestAction<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannel distributionChannel) {
        super(distributionChannel);
    }

}
