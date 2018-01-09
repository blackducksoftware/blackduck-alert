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
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class SlackDistributionConfigControllerTest extends ControllerTest<SlackDistributionConfigEntity, SlackDistributionRestModel, SlackDistributionRepository> {

    @Autowired
    SlackDistributionRepository slackDistributionRepository;

    @Override
    public SlackDistributionRepository getEntityRepository() {
        return slackDistributionRepository;
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getEntityMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockRestModelUtil<SlackDistributionRestModel> getRestModelMockUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/slack";
    }

}
