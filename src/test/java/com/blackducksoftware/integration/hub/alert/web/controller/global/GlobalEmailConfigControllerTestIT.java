package com.blackducksoftware.integration.hub.alert.web.controller.global;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalEmailConfigRestModel;

public class GlobalEmailConfigControllerTestIT extends GlobalControllerTest<GlobalEmailConfigEntity, GlobalEmailConfigRestModel, GlobalEmailRepository> {

    @Autowired
    GlobalEmailRepository globalEmailRepository;

    @Override
    public GlobalEmailRepository getGlobalEntityRepository() {
        return globalEmailRepository;
    }

    @Override
    public MockEmailGlobalEntity getGlobalEntityMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public MockEmailGlobalRestModel getGlobalRestModelMockUtil() {
        return new MockEmailGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/global/email";
    }

}
