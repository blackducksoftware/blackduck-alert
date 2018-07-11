package com.blackducksoftware.integration.alert.web.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.alert.mock.model.MockCommonDistributionRestModel;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;

public class CommonDistributionConfigControllerTestIT extends ControllerTest<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> {

    @Autowired
    CommonDistributionRepository commonDistributionRepository;

    @Override
    public CommonDistributionRepository getEntityRepository() {
        return commonDistributionRepository;
    }

    @Override
    public MockCommonDistributionEntity getEntityMockUtil() {
        return new MockCommonDistributionEntity();
    }

    @Override
    public MockCommonDistributionRestModel getRestModelMockUtil() {
        return new MockCommonDistributionRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/common";
    }

    @Test
    @Override
    public void testPostConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

    @Test
    @Override
    public void testDeleteConfig() throws Exception {
        entityRepository.deleteAll();
        final CommonDistributionConfigEntity savedEntity = entityRepository.save(entity);
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        restModel.setId(String.valueOf(savedEntity.getId()));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @Override
    public void testPutConfig() throws Exception {
        entityRepository.deleteAll();
        final CommonDistributionConfigEntity savedEntity = entityRepository.save(entity);
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        restModel.setDistributionConfigId(String.valueOf(savedEntity.getId()));
        restModel.setId(String.valueOf(savedEntity.getId()));
        final String content = gson.toJson(restModel);
        request.content(content);
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }
}
