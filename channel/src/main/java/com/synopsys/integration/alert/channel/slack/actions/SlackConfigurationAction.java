package com.synopsys.integration.alert.channel.slack.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class SlackConfigurationAction extends ConfigurationAction {

    @Autowired
    protected SlackConfigurationAction(SlackJobDetailsProcessor slackJobDetailsProcessor) {
        super(ChannelKeys.SLACK);
        addJobDetailsProcessor(slackJobDetailsProcessor);
    }
}
