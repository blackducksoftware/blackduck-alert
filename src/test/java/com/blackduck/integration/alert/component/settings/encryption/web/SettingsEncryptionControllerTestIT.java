package com.blackduck.integration.alert.component.settings.encryption.web;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.alert.util.AlertIntegrationTestConstants;
import com.google.gson.Gson;

@AlertIntegrationTest
class SettingsEncryptionControllerTestIT {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    private Gson gson;
    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testCreate() throws Exception {
        String url = AlertRestConstants.SETTINGS_ENCRYPTION_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testGetOne() throws Exception {
        String url = AlertRestConstants.SETTINGS_ENCRYPTION_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testUpdate() throws Exception {
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        String url = AlertRestConstants.SETTINGS_ENCRYPTION_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsEncryptionModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testValidate() throws Exception {
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel("password", Boolean.FALSE, "globalSalt", Boolean.FALSE, false);

        String url = AlertRestConstants.SETTINGS_ENCRYPTION_PATH + "/validate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsEncryptionModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testDelete() throws Exception {
        String url = AlertRestConstants.SETTINGS_ENCRYPTION_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }
}
