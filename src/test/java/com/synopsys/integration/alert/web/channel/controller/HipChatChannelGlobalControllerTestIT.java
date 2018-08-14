package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;

public class HipChatChannelGlobalControllerTestIT extends GlobalControllerTest<HipChatGlobalConfigEntity, HipChatGlobalConfig, HipChatGlobalRepository> {

    @Autowired
    HipChatGlobalRepository hipChatGlobalRepository;

    @Override
    public HipChatGlobalRepository getGlobalEntityRepository() {
        return hipChatGlobalRepository;
    }

    @Override
    public MockHipChatGlobalEntity getGlobalEntityMockUtil() {
        return new MockHipChatGlobalEntity();
    }

    @Override
    public MockHipChatGlobalRestModel getGlobalRestModelMockUtil() {
        return new MockHipChatGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/global/channel_hipchat";
    }

}
