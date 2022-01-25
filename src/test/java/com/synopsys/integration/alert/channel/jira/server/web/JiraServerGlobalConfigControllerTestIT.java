package com.synopsys.integration.alert.channel.jira.server.web;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Disabled("Actions have not yet been implemented, must wait for those before tests can be ran")
@AlertIntegrationTest
public class JiraServerGlobalConfigControllerTestIT {
    private static final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private static final String REQUEST_URL = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyGetOneEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + UUID.randomUUID();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyGetPageEndpointTest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        String urlPath = REQUEST_URL + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyCreateEndpointTest() throws Exception {
        String urlPath = REQUEST_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyUpdateEndpointTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);
        String urlPath = REQUEST_URL + uuid;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyValidateEndpointTest() throws Exception {
        String urlPath = REQUEST_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyDeleteEndpointTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);

        String urlPath = REQUEST_URL + uuid;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyTestEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "test";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyDisablePluginEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "disable-plugin";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private JiraServerGlobalConfigModel createConfigModel(UUID uuid) {
        return new JiraServerGlobalConfigModel(
            uuid.toString(),
            "Configuration name",
            DateUtils.createCurrentDateString(DateUtils.AUDIT_DATE_FORMAT),
            DateUtils.createCurrentDateString(DateUtils.AUDIT_DATE_FORMAT),
            "url",
            "username"
        );
    }
}
