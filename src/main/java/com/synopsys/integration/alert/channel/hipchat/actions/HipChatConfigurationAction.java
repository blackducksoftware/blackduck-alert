package com.synopsys.integration.alert.channel.hipchat.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.action.ConfigurationAction;

@Component
public class HipChatConfigurationAction extends ConfigurationAction {

    @Autowired
    protected HipChatConfigurationAction(final HipChatGlobalTestAction hipChatGlobalTestAction, final HipChatDistributionTestAction hipChatDistributionTestAction) {
        super(HipChatChannel.COMPONENT_NAME);
        addGlobalTestAction(hipChatGlobalTestAction);
        addDistributionTestAction(hipChatDistributionTestAction);
    }
}
