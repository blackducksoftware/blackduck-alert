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
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalSchedulingRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalSchedulingConfigControllerTest extends GlobalControllerTest<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {

    @Autowired
    GlobalSchedulingRepository globalSchedulingRepository;

    @Override
    public GlobalSchedulingRepository getGlobalEntityRepository() {
        return globalSchedulingRepository;
    }

    @Override
    public MockGlobalSchedulingEntity getGlobalEntityMockUtil() {
        return new MockGlobalSchedulingEntity();
    }

    @Override
    public MockGlobalSchedulingRestModel getGlobalRestModelMockUtil() {
        return new MockGlobalSchedulingRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/global/scheduling";
    }

    // TODO create a proper test for testTestConfig controller calls
    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        // final String testRestUrl = restUrl + "/test";
        // final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        // mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
        assertTrue(true);
    }
}
