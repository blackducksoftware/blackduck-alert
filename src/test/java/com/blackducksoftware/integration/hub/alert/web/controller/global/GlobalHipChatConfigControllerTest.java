package com.blackducksoftware.integration.hub.alert.web.controller.global;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalHipChatConfigControllerTest extends GlobalControllerTest<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> {

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
