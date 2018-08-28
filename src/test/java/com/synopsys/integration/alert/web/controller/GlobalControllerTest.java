package com.synopsys.integration.alert.web.controller;

import java.nio.charset.Charset;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;
import com.synopsys.integration.test.annotation.ExternalConnectionTest;

@Category({ DatabaseConnectionTest.class, ExternalConnectionTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class GlobalControllerTest {

    protected final TestProperties testProperties = new TestProperties();
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ContentConverter contentConverter;

    protected MockMvc mockMvc;

    protected Gson gson;

    protected RepositoryAccessor repositoryAccessor;

    protected String restUrl;

    protected DatabaseEntity entity;

    protected Config config;

    public abstract RepositoryAccessor getGlobalRepositoryAccessor();

    public abstract DatabaseEntity getGlobalEntity();

    public abstract Config getGlobalConfig();

    public abstract String getRestControllerUrl();

    @Before
    public void setup() {
        gson = new Gson();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        repositoryAccessor = getGlobalRepositoryAccessor();

        // delete all old data before the next test.
        // if there is data in the DB from another test, then it may cause the test to fail to post if there are items in the database.
        // this helps ensure that the environment is clean for the test.
        final List<? extends DatabaseEntity> entityList = repositoryAccessor.readEntities();
        entityList.forEach(entity -> getGlobalRepositoryAccessor().deleteEntity(entity.getId()));
        
        config = getGlobalConfig();
        entity = getGlobalEntity();
        entity = repositoryAccessor.saveEntity(entity);

        restUrl = BaseController.BASE_PATH + getRestControllerUrl();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(restUrl)
                                                      .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                      .with(SecurityMockMvcRequestPostProcessors.csrf());
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
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(restUrl)
                                                      .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                      .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        config.setId(String.valueOf(entity.getId()));
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        final String deleteUrl = restUrl + "?id=" + entity.getId();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(deleteUrl)
                                                      .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                      .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                      .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                      .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidateConfig() throws Exception {
        final String testRestUrl = restUrl + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                      .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                      .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
