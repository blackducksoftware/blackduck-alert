/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.blackduck.integration.alert.channel.msteams.distribution.MSTeamsChannel;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;

@Component
public class MSTeamsDistributionTestAction extends DistributionChannelMessageTestAction<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionTestAction(MsTeamsKey channelKey, MSTeamsChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}
