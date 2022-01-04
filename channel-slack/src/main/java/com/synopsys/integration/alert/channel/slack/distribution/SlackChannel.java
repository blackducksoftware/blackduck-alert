/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.MessageBoardChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackChannel extends MessageBoardChannel<SlackJobDetailsModel, SlackChannelMessageModel> {
    @Autowired
    protected SlackChannel(SlackChannelMessageConverter slackChannelMessageConverter, SlackChannelMessageSender slackChannelMessageSender) {
        super(slackChannelMessageConverter, slackChannelMessageSender);
    }

}
