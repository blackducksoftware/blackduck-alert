package com.synopsys.integration.alert.channel.azure.boards.web;

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

import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@AlertIntegrationTest
class AzureBoardsGlobalConfigControllerTestIT {
    private static final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private static final String REQUEST_URL = AlertRestConstants.AZURE_BOARDS_CONFIGURATION_PATH;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @BeforeEach
    @AfterEach
    public void cleanup() {
        azureBoardsGlobalConfigAccessor.getConfigurationPage(0, 100, null, null, null)
            .getModels()
            .stream()
            .map(AzureBoardsGlobalConfigModel::getId)
            .map(UUID::fromString)
            .forEach(azureBoardsGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetOneEndpointTest() throws Exception {
        AzureBoardsGlobalConfigModel savedConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));
        String urlPath = REQUEST_URL + "/" + savedConfigModel.getId();
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
        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&searchTerm=aname&sortName=name&sortOrder=asc", REQUEST_URL, 0, 10);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetPageWithSortAscendingEndpointTest() throws Exception {
        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&sortName=name&sortOrder=asc", REQUEST_URL, 0, 10);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyGetPageWithSortDescendingEndpointTest() throws Exception {
        String urlPath = String.format("%s?pageNumber=%s&pageSize=%s&sortName=name&sortOrder=desc", REQUEST_URL, 0, 10);
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

        AzureBoardsGlobalConfigModel configModel = createConfigModel(null);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        ResultActions resultActions = mockMvc.perform(request);
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyUpdateEndpointTest() throws Exception {
        AzureBoardsGlobalConfigModel configModel = createConfigModel(UUID.randomUUID());
        AzureBoardsGlobalConfigModel savedConfigModel = saveConfigModel(configModel);

        String urlPath = REQUEST_URL + "/" + savedConfigModel.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        request.content(gson.toJson(savedConfigModel));
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
        AzureBoardsGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyDeleteEndpointTest() throws Exception {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = saveConfigModel(createConfigModel(UUID.randomUUID()));

        String urlPath = REQUEST_URL + "/" + azureBoardsGlobalConfigModel.getId();
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
        AzureBoardsGlobalConfigModel configModel = createConfigModel(uuid);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void verifyAuthenticateEndpointTest() throws Exception {
        String urlPath = REQUEST_URL + "/oauth/authenticate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        AzureBoardsGlobalConfigModel configModel = createConfigModel(uuid);
        AzureBoardsGlobalConfigModel savedConfigModel = saveConfigModel(configModel);
        request.content(gson.toJson(savedConfigModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private AzureBoardsGlobalConfigModel createConfigModel(UUID uuid) {
        return new AzureBoardsGlobalConfigModel(
            (null != uuid) ? uuid.toString() : null,
            "Configuration name",
            "Organization name",
            "app id",
            "client secret"
        );
    }

    private AzureBoardsGlobalConfigModel saveConfigModel(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) throws AlertConfigurationException {
        return azureBoardsGlobalConfigAccessor.createConfiguration(azureBoardsGlobalConfigModel);
    }
}
