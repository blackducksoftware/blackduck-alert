package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.controller.ControllerTest;

public class SlackChannelDistributionControllerTestIT extends ControllerTest {

    @Autowired
    private SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;

    @Override
    public SlackDistributionRepositoryAccessor getRepositoryAccessor() {
        return slackDistributionRepositoryAccessor;
    }

    @Override
    public SlackDistributionConfigEntity getEntity() {
        return new MockSlackEntity().createEntity();
    }

    @Override
    public SlackDistributionConfig getConfig() {
        final MockSlackRestModel mockSlackRestModel = new MockSlackRestModel();
        mockSlackRestModel.setWebhook(testProperties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        mockSlackRestModel.setChannelName(testProperties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        return mockSlackRestModel.createRestModel();
    }

    @Override
    public String getDescriptorName() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Override
    public Long saveGlobalConfig() {
        return -1L;
    }

    @Override
    public void deleteGlobalConfig(final long id) {
    }

}
