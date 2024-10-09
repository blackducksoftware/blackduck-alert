/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.blackduck.integration.alert.channel.slack.distribution.SlackChannel;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackDistributionTestAction extends DistributionChannelMessageTestAction<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionTestAction(SlackChannelKey channelKey, SlackChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}
