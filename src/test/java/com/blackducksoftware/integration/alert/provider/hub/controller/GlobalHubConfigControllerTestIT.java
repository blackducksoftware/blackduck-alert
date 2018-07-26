package com.blackducksoftware.integration.alert.provider.hub.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;
import com.blackducksoftware.integration.alert.mock.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigActions;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigRestModel;

public class GlobalHubConfigControllerTestIT extends GlobalControllerTest<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> {

    @Autowired
    GlobalHubRepository globalHubRepository;

    @Autowired
    GlobalHubConfigActions globalHubConfigActions;

    @Autowired
    GlobalProperties globalProperties;

    @Override
    public GlobalHubRepository getGlobalEntityRepository() {
        return globalHubRepository;
    }

    @Override
    public MockGlobalEntityUtil<GlobalHubConfigEntity> getGlobalEntityMockUtil() {
        return new MockGlobalHubEntity();
    }

    @Override
    public MockGlobalRestModelUtil<GlobalHubConfigRestModel> getGlobalRestModelMockUtil() {
        return new MockGlobalHubRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/provider/hub";
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GlobalHubConfigEntity savedEntity = globalEntityRepository.save(entity);
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(restUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        restModel.setId(String.valueOf(savedEntity.getId()));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {

        final String hubUrl = testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL);
        final String timeout = testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT);
        final String apiKey = testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY);
        final String alwaysTrust = testProperties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        final String testRestUrl = restUrl + "/test";
        globalProperties.setHubUrl(hubUrl);
        globalProperties.setHubTrustCertificate(Boolean.valueOf(alwaysTrust));
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        final GlobalHubConfigRestModel hubRestModel = new GlobalHubConfigRestModel(null, hubUrl, String.valueOf(timeout), apiKey, false, null, null, null, null, false, "true");
        request.content(gson.toJson(hubRestModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(true);
        globalProperties.setHubUrl(null);
        globalProperties.setHubTrustCertificate(Boolean.FALSE);
    }
}
