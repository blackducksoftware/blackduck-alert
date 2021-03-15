/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.action.DistributionChannelTestAction;
import com.synopsys.integration.alert.channel.slack.distribution.SlackChannelV2;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackDistributionTestAction extends DistributionChannelTestAction<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionTestAction(SlackChannelV2 distributionChannel) {
        super(distributionChannel);
    }

}
