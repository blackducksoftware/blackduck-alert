package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class SlackChannelManagerTest extends ChannelManagerTest<SlackDistributionRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity, SlackManager> {

    @Override
    public SlackManager getChannelManager() {
        final SlackChannel mockSlackChannel = Mockito.mock(SlackChannel.class);
        final GlobalSlackRepository mockGlobalRepositoryWrapper = Mockito.mock(GlobalSlackRepository.class);
        final SlackDistributionRepository mockSlackRepositoryWrapper = Mockito.mock(SlackDistributionRepository.class);
        final SlackManager manager = new SlackManager(mockSlackChannel, mockGlobalRepositoryWrapper, mockSlackRepositoryWrapper, new ObjectTransformer(), contentConverter);

        return manager;
    }

    @Override
    public String getSupportedChannelName() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Override
    public String channelTopic() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Override
    public MockSlackEntity getMockEntityUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockGlobalEntityUtil<GlobalSlackConfigEntity> getMockGlobalEntityUtil() {
        return null;
    }

    @Override
    public MockSlackRestModel getMockRestModelUtil() {
        return new MockSlackRestModel();
    }

}
