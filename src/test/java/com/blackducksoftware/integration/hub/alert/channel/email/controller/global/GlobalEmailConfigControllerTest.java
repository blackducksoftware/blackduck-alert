package com.blackducksoftware.integration.hub.alert.channel.email.controller.global;

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
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalEmailConfigControllerTest extends GlobalControllerTest<GlobalEmailConfigEntity, GlobalEmailConfigRestModel, GlobalEmailRepository> {

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
