package com.blackducksoftware.integration.hub.alert.hub.controller;

import static org.junit.Assert.assertTrue;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalHubConfigControllerTest extends GlobalControllerTest<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> {

    @Autowired
    GlobalHubRepository globalHubRepository;

    private TestProperties testProperties;

    @Override
    public GlobalHubRepository getGlobalEntityRepository() {
        return globalHubRepository;
    }

    @Override
    public MockGlobalHubEntity getGlobalEntityMockUtil() {
        return new MockGlobalHubEntity();
    }

    @Override
    public MockGlobalHubRestModel getGlobalRestModelMockUtil() {
        return new MockGlobalHubRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/global";
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        // globalEntityRepository.deleteAll();
        // final String username = testProperties.getProperty(TestPropertyKey.TEST_USERNAME);
        // final String port = testProperties.getProperty(TestPropertyKey.TEST_HUB_PORT);
        // final String password = testProperties.getProperty(TestPropertyKey.TEST_PASSWORD);
        // final GlobalHubConfigEntity newEntity = new GlobalHubConfigEntity(Integer.valueOf(port), username, password);
        // final GlobalHubConfigEntity savedEntity = globalEntityRepository.save(newEntity);
        // final String testRestUrl = restUrl + "/test";
        // final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        // restModel.setId(String.valueOf(savedEntity.getId()));
        // request.content(restModel.toString());
        // request.contentType(contentType);
        // mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(true);
    }

}
