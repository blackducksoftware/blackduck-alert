package com.synopsys.integration.alert.component.settings.proxy.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Transactional
@AlertIntegrationTest
class SettingsProxyControllerTestIT {
    private static final String HOST = "hostname";
    private static final Integer PORT = 12345;
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "myPassword";
    private static final String NON_PROXY_HOSTNAME = "nonProxyHosts";

    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    private Gson gson;
    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;
    @Autowired
    private SettingsProxyConfigurationRepository settingsProxyConfigurationRepository;
    @Autowired
    private NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @AfterEach
    public void cleanup() {
        settingsProxyConfigurationRepository.deleteAllInBatch();
        settingsProxyConfigurationRepository.flush();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testCreate() throws Exception {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testCreateTwice() throws Exception {
        createDefaultSettingsProxyModel().orElseThrow(AssertionFailedError::new);
        SettingsProxyModel newSettingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "newHostname", 678);

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(newSettingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testGetOne() throws Exception {
        createDefaultSettingsProxyModel().orElseThrow(AssertionFailedError::new);

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testUpdate() throws Exception {
        createDefaultSettingsProxyModel().orElseThrow(AssertionFailedError::new);
        SettingsProxyModel newSettingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "newHostname", 678);

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(newSettingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testDelete() throws Exception {
        createDefaultSettingsProxyModel().orElseThrow(AssertionFailedError::new);

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testValidate() throws Exception {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();

        String url = AlertRestConstants.SETTINGS_PROXY_PATH + "/validate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testTest() throws Exception {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();
        String testUrl = "https://google.com";

        String url = AlertRestConstants.SETTINGS_PROXY_PATH + "/test" + "?testUrl=" + testUrl;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testNonProxyHostsDeleted() throws Exception {
        createDefaultSettingsProxyModel();

        SettingsProxyModel newSettingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "newHostname", 678);

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(newSettingsProxyModel))
            .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());

        String urlGet = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder requestGet = MockMvcRequestBuilders.get(new URI(urlGet))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        MvcResult mvcResult = mockMvc.perform(requestGet).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        TypeToken<SettingsProxyModel> settingsProxyModelType = new TypeToken<>() {};
        SettingsProxyModel updatedSettingsProxyModel = gson.fromJson(response, settingsProxyModelType.getType());

        //Non Proxy hosts will come back present, but contain nothing in the list
        assertTrue(updatedSettingsProxyModel.getNonProxyHosts().isPresent());
        assertTrue(updatedSettingsProxyModel.getNonProxyHosts().get().isEmpty());
    }

    private SettingsProxyModel createSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, HOST, PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setProxyPassword(PASSWORD);
        settingsProxyModel.setNonProxyHosts(List.of(NON_PROXY_HOSTNAME));
        return settingsProxyModel;
    }

    private Optional<SettingsProxyModel> createDefaultSettingsProxyModel() throws Exception {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();

        String url = AlertRestConstants.SETTINGS_PROXY_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(settingsProxyModel))
            .contentType(contentType);
        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        TypeToken<SettingsProxyModel> settingsProxyModelType = new TypeToken<>() {};
        SettingsProxyModel newSettingsProxyModel = gson.fromJson(response, settingsProxyModelType.getType());
        return Optional.of(newSettingsProxyModel);
    }
}
