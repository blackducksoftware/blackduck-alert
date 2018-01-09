package com.blackducksoftware.integration.hub.alert.web.controller.global;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalSchedulingRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;

public class GlobalSchedulingConfigControllerTestIT extends GlobalControllerTest<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {

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
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
