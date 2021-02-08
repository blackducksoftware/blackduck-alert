package com.synopsys.integration.alert.channel.slack2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopys.integration.alert.channel.api.MessageBoardChannel;

@Component
public class SlackChannelV2 extends MessageBoardChannel<SlackJobDetailsModel, SlackChannelMessageModel> {
    @Autowired
    protected SlackChannelV2(SlackChannelMessageConverter slackChannelMessageConverter, SlackChannelMessageSender slackChannelMessageSender) {
        super(slackChannelMessageConverter, slackChannelMessageSender);
    }
    
}
