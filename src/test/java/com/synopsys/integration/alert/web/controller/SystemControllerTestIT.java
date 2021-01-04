package com.synopsys.integration.alert.web.controller;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;
import com.synopsys.integration.alert.web.api.system.SystemActions;

@AlertIntegrationTest
public class SystemControllerTestIT {
    private static final String SYSTEM_MESSAGE_BASE_URL = AlertRestConstants.BASE_PATH + "/system/messages";
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private final Gson gson = new Gson();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private SystemActions systemActions;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        systemActions = Mockito.mock(SystemActions.class);
    }

    @Test
    public void testGetLatestMessages() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL + "/latest")
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetMessages() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
