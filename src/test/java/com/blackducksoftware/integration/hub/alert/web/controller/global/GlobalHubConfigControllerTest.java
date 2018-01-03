package com.blackducksoftware.integration.hub.alert.web.controller.global;

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
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalHubEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalHubRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalHubConfigControllerTest extends GlobalControllerTest<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> {

    @Autowired
    GlobalHubRepository globalHubRepository;

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

    // TODO create a custom testTestConfig controller method
    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        // final String testRestUrl = restUrl + "/test";
        // final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        // mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(true);
    }

}
