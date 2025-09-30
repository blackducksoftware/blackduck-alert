/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.web;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.alert.util.AlertIntegrationTestConstants;
import com.google.gson.Gson;

@AlertIntegrationTest
class JiraServerGlobalConfigControllerTestIT {
    private static final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private static final String REQUEST_URL = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;
    private static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;

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
        jiraServerGlobalConfigAccessor.getConfigurationPage(0, 100, null, null, null)
            .getModels()
            .stream()
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString)
            .forEach(jiraServerGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetOneEndpointTest() throws Exception {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));
        String urlPath = REQUEST_URL + "/" + jiraServerGlobalConfigModel.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetPageEndpointTest() throws Exception {
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
    void verifyGetPageWithSearchTermEndpointTest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&searchTerm=aname&sortName=name&sortOrder=asc", REQUEST_URL, pageNumber, pageSize);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetPageWithSortAscendingEndpointTest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&sortName=name&sortOrder=asc", REQUEST_URL, pageNumber, pageSize);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetPageWithSortDescendingEndpointTest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&sortName=name&sortOrder=desc", REQUEST_URL, pageNumber, pageSize);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyCreateEndpointTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
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
    void verifyCreateEndpointWithPersonalAccessTokenTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        JiraServerGlobalConfigModel configModel = createPersonalAccessTokenConfigModel(null, "https://blackduck.com");
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        ResultActions resultActions = mockMvc.perform(request);
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyUpdateEndpointTest() throws Exception {
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
    void verifyValidateEndpointTest() throws Exception {
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
    void verifyDeleteEndpointTest() throws Exception {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));

        String urlPath = REQUEST_URL + "/" + jiraServerGlobalConfigModel.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyTestEndpointTest() throws Exception {
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
    void verifyDisablePluginEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "/install-plugin";
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
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName("username");
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel createPersonalAccessTokenConfigModel(UUID uuid, String url) {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            (null != uuid) ? uuid.toString() : null,
            "Configuration name",
            url,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN
        );
        jiraServerGlobalConfigModel.setAccessToken("accessToken");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel saveConfigModel(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) throws AlertConfigurationException {
        return jiraServerGlobalConfigAccessor.createConfiguration(jiraServerGlobalConfigModel);
    }
}
