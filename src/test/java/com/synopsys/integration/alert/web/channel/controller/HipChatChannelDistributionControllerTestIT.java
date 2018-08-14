package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.alert.web.controller.ControllerTest;

public class HipChatChannelDistributionControllerTestIT extends ControllerTest<HipChatDistributionConfigEntity, HipChatDistributionConfig, HipChatDistributionRepository> {

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
    public MockRestModelUtil<HipChatDistributionConfig> getRestModelMockUtil() {
        return new MockHipChatRestModel();
    }

    @Override
    public String getDescriptorName() {
        return HipChatChannel.COMPONENT_NAME;
    }

}
