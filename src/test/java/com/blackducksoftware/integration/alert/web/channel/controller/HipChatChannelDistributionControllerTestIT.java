package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRestModel;
import com.blackducksoftware.integration.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.controller.ControllerTest;

public class HipChatChannelDistributionControllerTestIT extends ControllerTest<HipChatDistributionConfigEntity, HipChatDistributionRestModel, HipChatDistributionRepository> {

    @Autowired
    HipChatDistributionRepository hipChatDistributionRepository;

    @Override
    public HipChatDistributionRepository getEntityRepository() {
        return hipChatDistributionRepository;
    }

    @Override
    public MockEntityUtil<HipChatDistributionConfigEntity> getEntityMockUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public MockRestModelUtil<HipChatDistributionRestModel> getRestModelMockUtil() {
        return new MockHipChatRestModel();
    }

    @Override
    public String getDescriptorName() {
        return "/configuration/channel/distribution/channel_hipchat";
    }

}
