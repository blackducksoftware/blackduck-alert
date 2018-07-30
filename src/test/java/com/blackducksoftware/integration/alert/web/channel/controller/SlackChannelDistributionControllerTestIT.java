package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.channel.model.SlackDistributionConfig;
import com.blackducksoftware.integration.alert.web.controller.ControllerTest;

public class SlackChannelDistributionControllerTestIT extends ControllerTest<SlackDistributionConfigEntity, SlackDistributionConfig, SlackDistributionRepository> {

    @Autowired
    private SlackDistributionRepository slackDistributionRepository;

    @Override
    public SlackDistributionRepository getEntityRepository() {
        return slackDistributionRepository;
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getEntityMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockRestModelUtil<SlackDistributionConfig> getRestModelMockUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public String getDescriptorName() {
        return SlackChannel.COMPONENT_NAME;
    }

}
