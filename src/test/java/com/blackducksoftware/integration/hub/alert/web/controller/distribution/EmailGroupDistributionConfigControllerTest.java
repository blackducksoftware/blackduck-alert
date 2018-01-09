package com.blackducksoftware.integration.hub.alert.web.controller.distribution;

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
import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class EmailGroupDistributionConfigControllerTest extends ControllerTest<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel, EmailGroupDistributionRepository> {

    @Autowired
    EmailGroupDistributionRepository emailGroupDistributionRepository;

    @Override
    public EmailGroupDistributionRepository getEntityRepository() {
        return emailGroupDistributionRepository;
    }

    @Override
    public MockEmailEntity getEntityMockUtil() {
        return new MockEmailEntity();
    }

    @Override
    public MockEmailRestModel getRestModelMockUtil() {
        return new MockEmailRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/emailGroup";
    }

}
