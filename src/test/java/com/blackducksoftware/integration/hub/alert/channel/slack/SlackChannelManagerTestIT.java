package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;

public class SlackChannelManagerTestIT extends ChannelManagerTest<SlackDistributionRestModel, SlackDistributionConfigEntity, GlobalChannelConfigEntity> {

    @Autowired
    private SlackDistributionRepository distributionRepository;

    @Autowired
    private SlackDescriptor slackDescriptor;

    @Override
    public void cleanGlobalRepository() {
        // do nothing no global configuration
    }

    @Override
    public void saveGlobalConfiguration() {
        // do nothing no global configuration
    }

    @Override
    public void cleanDistributionRepository() {
        distributionRepository.deleteAll();
    }

    @Override
    public MockSlackRestModel getMockRestModelUtil() {
        final MockSlackRestModel restModel = new MockSlackRestModel();
        restModel.setChannelName(this.properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        restModel.setChannelUsername(this.properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
        restModel.setWebhook(this.properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        restModel.setId("");
        return restModel;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return slackDescriptor;
    }

}
