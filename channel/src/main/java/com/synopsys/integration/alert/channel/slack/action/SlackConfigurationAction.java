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

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class SlackConfigurationAction extends ConfigurationAction {
    @Autowired
    protected SlackConfigurationAction(SlackJobDetailsExtractor slackJobDetailsExtractor, SlackDistributionTestAction slackDistributionTestAction) {
        super(ChannelKeys.SLACK);
        addJobDetailsExtractor(slackJobDetailsExtractor);
        addDistributionTestAction(slackDistributionTestAction);
    }

}
