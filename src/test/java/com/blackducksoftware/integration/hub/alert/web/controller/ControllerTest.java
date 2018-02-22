package com.blackducksoftware.integration.hub.alert.web.controller;

import java.nio.charset.Charset;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category({ DatabaseConnectionTest.class, ExternalConnectionTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class ControllerTest<E extends DatabaseEntity, R extends CommonDistributionConfigRestModel, CR extends JpaRepository<E, Long>> {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected CommonDistributionRepository commonDistributionRepository;

    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    protected MockMvc mockMvc;

    protected CR entityRepository;

    protected MockEntityUtil<E> entityMockUtil;

    protected MockRestModelUtil<R> restModelMockUtil;

    protected MockCommonDistributionEntity distributionMockUtil;

    protected String restUrl;

    protected E entity;

    protected R restModel;

    public abstract CR getEntityRepository();

    public abstract MockEntityUtil<E> getEntityMockUtil();

    public abstract MockRestModelUtil<R> getRestModelMockUtil();

    public abstract String getRestControllerUrl();

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        entityRepository = getEntityRepository();
        entityRepository.deleteAllInBatch();

        entityMockUtil = getEntityMockUtil();
        restModelMockUtil = getRestModelMockUtil();
        distributionMockUtil = new MockCommonDistributionEntity();

        restModel = restModelMockUtil.createRestModel();
        entity = entityMockUtil.createEntity();
        entityRepository.save(entity);

        restUrl = "/api/" + getRestControllerUrl();
    }

    @After
    public void cleanup() {
        entityRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPostConfig() throws Exception {
        entityRepository.deleteAll();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.content(restModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPutConfig() throws Exception {
        entityRepository.deleteAll();
        final E savedEntity = entityRepository.save(entity);
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        restModel.setDistributionConfigId(String.valueOf(savedEntity.getId()));
        restModel.setId(String.valueOf(commonEntity.getId()));
        request.content(restModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(restUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        entityRepository.deleteAll();
        final E savedEntity = entityRepository.save(entity);
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        restModel.setDistributionConfigId(String.valueOf(savedEntity.getId()));
        restModel.setId(String.valueOf(commonEntity.getId()));
        request.content(restModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidConfig() throws Exception {
        entityRepository.deleteAll();
        final String testRestUrl = restUrl + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.content(restModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
