package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(ExternalConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HomeControllerTestIT {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final String homeVerifyUrl = "/api/verify";
    private final String homeUrl = "/";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerify() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", UUID.randomUUID().toString());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.headers(headers);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerifyMissingCSRFToken() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerifyNullStringCSRFToken() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", "null");
        request.headers(headers);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testIndex() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeUrl);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
