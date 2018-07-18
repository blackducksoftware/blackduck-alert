package com.blackducksoftware.integration.alert.scheduling.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.alert.scheduling.mock.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingConfigRestModel;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.scheduling.model.MockGlobalSchedulingRestModel;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;

public class GlobalSchedulingConfigControllerTestIT extends GlobalControllerTest<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {

    @Autowired
    GlobalSchedulingRepository globalSchedulingRepository;

    @Autowired
    GlobalSchedulingConfigActions globalSchedulingConfigActions;

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

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GlobalSchedulingConfigEntity savedEntity = globalEntityRepository.save(entity);
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        restModel.setId(String.valueOf(savedEntity.getId()));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

}
