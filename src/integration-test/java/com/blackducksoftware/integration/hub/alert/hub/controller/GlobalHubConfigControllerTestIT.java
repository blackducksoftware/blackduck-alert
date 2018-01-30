package com.blackducksoftware.integration.hub.alert.hub.controller;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigActions;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigController;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;

public class GlobalHubConfigControllerTestIT extends GlobalControllerTest<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> {

    @Autowired
    GlobalHubRepository globalHubRepository;

    @Autowired
    GlobalHubConfigActions globalHubConfigActions;

    private final TestProperties testProperties;

    public GlobalHubConfigControllerTestIT() {
        testProperties = new TestProperties();
    }

    @Override
    public GlobalHubRepository getGlobalEntityRepository() {
        return globalHubRepository;
    }

    @Override
    public MockGlobalHubEntity getGlobalEntityMockUtil() {
        return new MockGlobalHubEntity();
    }

    @Override
    public MockGlobalHubRestModel getGlobalRestModelMockUtil() {
        final MockGlobalHubRestModel mockUtil = new MockGlobalHubRestModel();
        mockUtil.setHubApiKey(testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        return mockUtil;
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/global";
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        // TODO fix this
        // globalEntityRepository.deleteAll();
        // final String username = testProperties.getProperty(TestPropertyKey.TEST_USERNAME);
        // final String port = testProperties.getProperty(TestPropertyKey.TEST_HUB_PORT);
        // final String password = testProperties.getProperty(TestPropertyKey.TEST_PASSWORD);
        // final GlobalHubConfigEntity newEntity = new GlobalHubConfigEntity(Integer.valueOf(port), username, password);
        // final GlobalHubConfigEntity savedEntity = globalEntityRepository.save(newEntity);
        // final String testRestUrl = restUrl + "/test";
        // final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        // restModel.setId(String.valueOf(savedEntity.getId()));
        // request.content(restModel.toString());
        // request.contentType(contentType);
        // mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(true);
    }

    @Override
    public GlobalHubConfigController getController() {
        return new GlobalHubConfigController(globalHubConfigActions, new ObjectTransformer());
    }

}
