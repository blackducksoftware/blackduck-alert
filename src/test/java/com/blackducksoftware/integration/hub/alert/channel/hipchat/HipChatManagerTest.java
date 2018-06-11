package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;

public class HipChatManagerTest extends ChannelManagerTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {

    @Override
    public String getDestination() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public MockHipChatEntity getMockEntityUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public MockHipChatGlobalEntity getMockGlobalEntityUtil() {
        return new MockHipChatGlobalEntity();
    }

    @Override
    public MockHipChatRestModel getMockRestModelUtil() {
        return new MockHipChatRestModel();
    }

}
