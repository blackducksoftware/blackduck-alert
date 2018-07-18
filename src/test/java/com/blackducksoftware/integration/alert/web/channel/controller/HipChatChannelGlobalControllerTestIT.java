package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalRepository;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;

public class HipChatChannelGlobalControllerTestIT extends GlobalControllerTest<HipChatGlobalConfigEntity, HipChatGlobalConfigRestModel, HipChatGlobalRepository> {

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
