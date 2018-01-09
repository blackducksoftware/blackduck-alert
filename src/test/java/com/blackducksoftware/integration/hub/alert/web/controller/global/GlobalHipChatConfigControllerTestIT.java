package com.blackducksoftware.integration.hub.alert.web.controller.global;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

public class GlobalHipChatConfigControllerTestIT extends GlobalControllerTest<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> {

    @Autowired
    private GlobalHipChatRepository globalHipChatRepository;

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

}
