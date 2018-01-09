package com.blackducksoftware.integration.hub.alert.web.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;

public class HipChatDistributionConfigControllerTestIT extends ControllerTest<HipChatDistributionConfigEntity, HipChatDistributionRestModel, HipChatDistributionRepository> {

    @Autowired
    HipChatDistributionRepository hipChatDistributionRepository;

    @Override
    public HipChatDistributionRepository getEntityRepository() {
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
