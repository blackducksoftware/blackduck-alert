package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class HipChatManagerTest extends ChannelManagerTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity, HipChatManager> {

    @Override
    public HipChatManager getChannelManager() {
        final HipChatChannel mockChannel = Mockito.mock(HipChatChannel.class);
        final GlobalHipChatRepository mockGlobalRepositoryWrapper = Mockito.mock(GlobalHipChatRepository.class);
        final HipChatDistributionRepository mockRepositoryWrapper = Mockito.mock(HipChatDistributionRepository.class);
        final HipChatManager manager = new HipChatManager(mockChannel, mockGlobalRepositoryWrapper, mockRepositoryWrapper, new ObjectTransformer(), contentConverter);

        return manager;
    }

    @Override
    public String getSupportedChannelName() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public String channelTopic() {
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
