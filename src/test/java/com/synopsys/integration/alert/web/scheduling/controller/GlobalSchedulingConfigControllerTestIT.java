package com.synopsys.integration.alert.web.scheduling.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingReposioryAccessor;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.scheduling.mock.MockGlobalSchedulingEntity;
import com.synopsys.integration.alert.web.scheduling.model.MockGlobalSchedulingRestModel;

public class GlobalSchedulingConfigControllerTestIT extends GlobalControllerTest {

    @Autowired
    SchedulingReposioryAccessor schedulingRepositoryAccessor;

    @Override
    public SchedulingReposioryAccessor getGlobalRepositoryAccessor() {
        return schedulingRepositoryAccessor;
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/component/component_scheduling";
    }

    @Override
    public DatabaseEntity getGlobalEntity() {
        return new MockGlobalSchedulingEntity().createGlobalEntity();
    }

    @Override
    public Config getGlobalConfig() {
        final MockGlobalSchedulingRestModel mockGlobalSchedulingRestModel = new MockGlobalSchedulingRestModel();
        mockGlobalSchedulingRestModel.setId("2");
        return mockGlobalSchedulingRestModel.createGlobalRestModel();
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        config.setId(String.valueOf(entity.getId()));
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

}
