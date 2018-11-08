package com.synopsys.integration.alert.web.controller;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public abstract class ControllerTest extends AlertIntegrationTest {
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    protected MockMvc mockMvc;
    protected Gson gson;
    protected RepositoryAccessor repositoryAccessor;
    protected MockCommonDistributionEntity distributionMockUtil;
    protected String restUrl;
    protected DatabaseEntity entity;
    protected CommonDistributionConfig config;
    protected TestProperties testProperties = new TestProperties();

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected CommonDistributionRepository commonDistributionRepository;

    @Autowired
    protected ContentConverter contentConverter;

    public abstract RepositoryAccessor getRepositoryAccessor();

    public abstract DatabaseEntity getEntity();

    public abstract CommonDistributionConfig getConfig();

    public abstract String getDescriptorName();

    public abstract Long saveGlobalConfig();

    public abstract void deleteGlobalConfig(long id);

    @Before
    public void setup() {
        gson = new Gson();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        commonDistributionRepository.deleteAll();
        repositoryAccessor = getRepositoryAccessor();
        distributionMockUtil = new MockCommonDistributionEntity();

        if (entity != null && repositoryAccessor.readEntity(entity.getId()).isPresent()) {
            repositoryAccessor.deleteEntity(entity.getId());
        }
        if (config != null && repositoryAccessor.readEntity(contentConverter.getLongValue(config.getId())).isPresent()) {
            repositoryAccessor.deleteEntity(contentConverter.getLongValue(config.getId()));
        }

        config = getConfig();
        entity = getEntity();
        entity = repositoryAccessor.saveEntity(entity);
        restUrl = BaseController.BASE_PATH + "/configuration/channel/distribution/" + getDescriptorName();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        distributionMockUtil.setDistributionType(getDescriptorName());
        distributionMockUtil.setDistributionConfigId(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final String getUrl = restUrl + "?id=" + commonEntity.getId();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPostConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(restUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPutConfig() throws Exception {
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(restUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        config.setDistributionConfigId(String.valueOf(entity.getId()));
        config.setId(String.valueOf(commonEntity.getId()));
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        distributionMockUtil.setDistributionConfigId(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final String deleteUrl = restUrl + "?id=" + commonEntity.getId();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(deleteUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final long id = saveGlobalConfig();
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        config.setDistributionConfigId(String.valueOf(entity.getId()));
        config.setId(String.valueOf(commonEntity.getId()));
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        deleteGlobalConfig(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidConfig() throws Exception {
        final String testRestUrl = restUrl + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
