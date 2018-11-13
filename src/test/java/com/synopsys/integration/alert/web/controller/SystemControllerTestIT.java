package com.synopsys.integration.alert.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.AlertIntegrationTest;

public class SystemControllerTestIT extends AlertIntegrationTest {
    private final String systemMessageBaseUrl = BaseController.BASE_PATH + "/system/messages";
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Before
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void testGetLatestMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemMessageBaseUrl + "/latest")
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getGetMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemMessageBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
