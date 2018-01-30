package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;

public class GlobalHipChatConfigControllerTestIT extends GlobalControllerTest<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> {

    @Autowired
    private GlobalHipChatRepository globalHipChatRepository;

    @Autowired
    GlobalHipChatConfigActions globalHipChatConfigActions;

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
        return "/configuration/global/hipchat";
    }

    @Override
    public ConfigController<GlobalHipChatConfigRestModel> getController() {
        return new GlobalHipChatConfigController(globalHipChatConfigActions, new ObjectTransformer());
    }

}
