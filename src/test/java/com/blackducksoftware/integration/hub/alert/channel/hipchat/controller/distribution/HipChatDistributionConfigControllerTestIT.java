package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;

public class HipChatDistributionConfigControllerTestIT extends ControllerTest<HipChatDistributionConfigEntity, HipChatDistributionRestModel, HipChatDistributionRepository, HipChatDistributionRepositoryWrapper> {

    @Autowired
    HipChatDistributionRepositoryWrapper hipChatDistributionRepository;

    @Override
    public HipChatDistributionRepositoryWrapper getEntityRepository() {
        return hipChatDistributionRepository;
    }

    @Override
    public MockHipChatEntity getEntityMockUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public MockHipChatRestModel getRestModelMockUtil() {
        return new MockHipChatRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/hipchat";
    }

}
