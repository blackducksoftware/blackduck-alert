package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;
import com.synopsys.integration.alert.web.model.Config;

public class HipChatChannelGlobalControllerTestIT extends GlobalControllerTest {

    @Autowired
    HipChatGlobalRepositoryAccessor hipChatGlobalRepositoryAccessor;

    @Override
    public HipChatGlobalRepositoryAccessor getGlobalRepositoryAccessor() {
        return hipChatGlobalRepositoryAccessor;
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/global/channel_hipchat";
    }

    @Override
    public DatabaseEntity getGlobalEntity() {
        final MockHipChatGlobalEntity mockGlobalEntity = new MockHipChatGlobalEntity();
        return mockGlobalEntity.createGlobalEntity();
    }

    @Override
    public Config getGlobalConfig() {
        final MockHipChatGlobalRestModel mockGlobalConfig = new MockHipChatGlobalRestModel();
        mockGlobalConfig.setApiKey(testProperties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));
        return mockGlobalConfig.createGlobalRestModel();
    }

}
