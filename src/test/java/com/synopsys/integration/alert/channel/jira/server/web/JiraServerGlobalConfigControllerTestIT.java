package com.synopsys.integration.alert.channel.jira.server.web;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@AlertIntegrationTest
public class JiraServerGlobalConfigControllerTestIT {
    private static final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private static final String REQUEST_URL = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @AfterEach
    public void cleanup() {
        jiraServerGlobalConfigAccessor.getConfigurationPage(0, 100)
            .getModels()
            .stream()
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString)
            .forEach(jiraServerGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyGetOneEndpointTest() throws Exception {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));
        String urlPath = REQUEST_URL + "/" + jiraServerGlobalConfigModel.getId();
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

        JiraServerGlobalConfigModel configModel = createConfigModel(null);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        ResultActions resultActions = mockMvc.perform(request);
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyUpdateEndpointTest() throws Exception {
        JiraServerGlobalConfigModel configModel = createConfigModel(UUID.randomUUID());
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = saveConfigModel(configModel);

        String urlPath = REQUEST_URL + "/" + jiraServerGlobalConfigModel.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        request.content(gson.toJson(jiraServerGlobalConfigModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyValidateEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "/validate";
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
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));

        String urlPath = REQUEST_URL + "/" + jiraServerGlobalConfigModel.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Disabled("Test action not yet implemented")
    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void verifyTestEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "/test";
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
        String urlPath = REQUEST_URL + "/disable-plugin";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        // Need to just verify the endpoint exists and not actually connect to Jira
        JiraServerGlobalConfigModel configModel = createConfigModel(uuid, "badUrl");
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private JiraServerGlobalConfigModel createConfigModel(UUID uuid) {
        return createConfigModel(uuid, "https://google.com");
    }

    private JiraServerGlobalConfigModel createConfigModel(UUID uuid, String url) {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            (null != uuid) ? uuid.toString() : null,
            "Configuration name",
            url,
            "username"
        );
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel saveConfigModel(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        JiraServerGlobalConfigModel configuration = jiraServerGlobalConfigAccessor.createConfiguration(jiraServerGlobalConfigModel);
        return configuration;
    }
}
