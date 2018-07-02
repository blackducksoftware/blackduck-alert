package com.blackducksoftware.integration.hub.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.slack.SlackDescriptor;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;

public class ChannelDistributionControllerTestIT extends ControllerTest<SlackDistributionConfigEntity, SlackDistributionRestModel, SlackDistributionRepository> {

    @Autowired
    private SlackDescriptor descriptor;

    @Override
    public SlackDistributionRepository getEntityRepository() {
        return descriptor.getDistributionRepository();
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getEntityMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockRestModelUtil<SlackDistributionRestModel> getRestModelMockUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/distribution/channel_slack";
    }

}
