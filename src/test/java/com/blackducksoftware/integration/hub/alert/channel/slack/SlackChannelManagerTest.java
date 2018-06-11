package com.blackducksoftware.integration.hub.alert.channel.slack;

import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;

public class SlackChannelManagerTest extends ChannelManagerTest<SlackDistributionRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity> {

    @Override
    public String getDestination() {
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
