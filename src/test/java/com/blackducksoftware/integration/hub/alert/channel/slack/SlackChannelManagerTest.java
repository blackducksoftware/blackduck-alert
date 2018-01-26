package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class SlackChannelManagerTest extends ChannelManagerTest<SlackEvent, SlackDistributionRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity, SlackManager> {

    @Override
    public SlackManager getChannelManager() {
        final SlackChannel mockSlackChannel = Mockito.mock(SlackChannel.class);
        final GlobalSlackRepositoryWrapper mockGlobalRepositoryWrapper = Mockito.mock(GlobalSlackRepositoryWrapper.class);
        final SlackDistributionRepositoryWrapper mockSlackRepositoryWrapper = Mockito.mock(SlackDistributionRepositoryWrapper.class);
        final SlackManager manager = new SlackManager(mockSlackChannel, mockGlobalRepositoryWrapper, mockSlackRepositoryWrapper, new ObjectTransformer());

        return manager;
    }

    @Override
    public String getSupportedChannelName() {
        return SupportedChannels.SLACK;
    }

    @Override
    public String channelTopic() {
        return SupportedChannels.SLACK;
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
