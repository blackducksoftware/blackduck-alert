package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.hub.alert.scheduling.mock.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;

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
    @WithMockUser(roles = "ADMIN")
    public void testRunAccumulator() throws Exception {
        globalEntityRepository.deleteAll();
        final String accumulatorRunRestUrl = restUrl + "/accumulator/run";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(accumulatorRunRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GlobalSchedulingConfigEntity savedEntity = globalEntityRepository.save(entity);
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        restModel.setId(String.valueOf(savedEntity.getId()));
        request.content(restModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

    @Override
    public ConfigController<GlobalSchedulingConfigRestModel> getController() {
        return new GlobalSchedulingConfigController(globalSchedulingConfigActions, new ObjectTransformer());
    }

}
