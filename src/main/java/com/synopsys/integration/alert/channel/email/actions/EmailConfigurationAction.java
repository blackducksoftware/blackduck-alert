package com.synopsys.integration.alert.channel.email.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.action.ConfigurationAction;

@Component
public class EmailConfigurationAction extends ConfigurationAction {

    @Autowired
    protected EmailConfigurationAction(final EmailGlobalTestAction emailGlobalTestAction, final EmailDistributionTestAction emailDistributionTestAction) {
        super(EmailChannel.COMPONENT_NAME);
        addGlobalTestAction(emailGlobalTestAction);
        addDistributionTestAction(emailDistributionTestAction);
    }
}
