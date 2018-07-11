package com.blackducksoftware.integration.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.alert.channel.slack.SlackDescriptor;
import com.blackducksoftware.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;

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
