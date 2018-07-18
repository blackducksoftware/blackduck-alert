package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatRepository;
import com.blackducksoftware.integration.alert.web.channel.model.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;

public class ChannelGlobalControllerTestIT extends GlobalControllerTest<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> {

    @Autowired
    GlobalHipChatRepository globalHipChatRepository;

    @Override
    public GlobalHipChatRepository getGlobalEntityRepository() {
        return globalHipChatRepository;
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
