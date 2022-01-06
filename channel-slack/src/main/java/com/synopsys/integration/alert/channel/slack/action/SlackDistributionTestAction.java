/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.synopsys.integration.alert.channel.slack.distribution.SlackChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

@Component
public class SlackDistributionTestAction extends DistributionChannelMessageTestAction<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionTestAction(SlackChannelKey channelKey, SlackChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}
