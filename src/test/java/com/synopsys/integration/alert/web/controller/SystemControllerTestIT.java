package com.synopsys.integration.alert.web.controller;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
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
import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.web.model.SystemSetupModel;
import com.synopsys.integration.alert.workflow.startup.install.SystemInitializer;

public class SystemControllerTestIT extends AlertIntegrationTest {
    private final String systemMessageBaseUrl = BaseController.BASE_PATH + "/system/messages";
    private final String systemInitialSetupBaseUrl = BaseController.BASE_PATH + "/system/setup/initial";
    private final Gson gson = new Gson();
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private SystemInitializer systemInitializer;
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
    public void testGetMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemMessageBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetInitialSystemSetup() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemInitialSetupBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        if (systemInitializer.isSystemInitialized()) {
            // the spring-test.properties file sets the encryption and in order to run a hub URL is needed therefore the environment is setup.
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isFound());
        } else {
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    @Test
    public void testPostInitialSystemSetup() throws Exception {
        final TestProperties testProperties = new TestProperties();
        final String blackDuckProviderUrl = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL);
        final Integer blackDuckConnectionTimeout = 300;
        final String blackDuckApiToken = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY);
        final boolean blackDuckApiTokenSet = false;
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = false;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = false;
        final String proxyHost = "";
        final String proxyPort = "";
        final String proxyUsername = "";
        final String proxyPassword = "";
        final boolean proxyPasswordSet = false;

        final SystemSetupModel configuration = SystemSetupModel.of(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, blackDuckApiTokenSet,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword, proxyPasswordSet);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(systemInitialSetupBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(configuration));
        request.contentType(contentType);
        if (systemInitializer.isSystemInitialized()) {
            // the spring-test.properties file sets the encryption and in order to run a hub URL is needed therefore the environment is setup.
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isConflict());
        } else {
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }
}
