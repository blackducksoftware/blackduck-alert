/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.synopsys.integration.alert.channel.msteams.distribution.MSTeamsChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;

@Component
public class MSTeamsDistributionTestAction extends DistributionChannelMessageTestAction<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionTestAction(MsTeamsKey channelKey, MSTeamsChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}
