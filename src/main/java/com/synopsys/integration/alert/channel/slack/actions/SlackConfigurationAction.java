package com.synopsys.integration.alert.channel.slack.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.action.ConfigurationAction;

@Component
public class SlackConfigurationAction extends ConfigurationAction {

    @Autowired
    protected SlackConfigurationAction(final SlackDistributionTestAction slackDistributionTestAction) {
        super(SlackChannel.COMPONENT_NAME);
        addDistributionTestAction(slackDistributionTestAction);
    }
}
