package com.blackducksoftware.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.common.AlertProperties;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;
import com.blackducksoftware.integration.alert.mock.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfigActions;

public class GlobalBlackDuckConfigControllerTestIT extends GlobalControllerTest<GlobalBlackDuckConfigEntity, GlobalBlackDuckConfig, GlobalBlackDuckRepository> {

    @Autowired
    GlobalBlackDuckRepository globalBlackDuckRepository;

    @Autowired
    GlobalBlackDuckConfigActions globalBlackDuckConfigActions;

    @Autowired
    BlackDuckProperties blackDuckProperties;

    @Autowired
    AlertProperties alertProperties;

    @Override
    public GlobalBlackDuckRepository getGlobalEntityRepository() {
        return globalBlackDuckRepository;
    }

    @Override
    public MockGlobalEntityUtil<GlobalBlackDuckConfigEntity> getGlobalEntityMockUtil() {
        return new MockGlobalBlackDuckEntity();
    }

    @Override
    public MockGlobalRestModelUtil<GlobalBlackDuckConfig> getGlobalRestModelMockUtil() {
        return new MockGlobalBlackDuckRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/provider/blackduck";
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GlobalBlackDuckConfigEntity savedEntity = globalEntityRepository.save(entity);
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
        ReflectionTestUtils.setField(blackDuckProperties, "blackDuckUrl", hubUrl);
        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(alwaysTrust));
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        final GlobalBlackDuckConfig hubRestModel = new GlobalBlackDuckConfig(null, hubUrl, String.valueOf(timeout), apiKey, false, null, null, null, null, false, "true");
        request.content(gson.toJson(hubRestModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(true);
        ReflectionTestUtils.setField(blackDuckProperties, "blackDuckUrl", null);
        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", false);
    }
}
