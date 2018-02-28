package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalControllerTest;

public class GlobalHipChatConfigControllerTestIT extends GlobalControllerTest<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> {

    @Autowired
    private GlobalHipChatRepository globalHipChatRepository;

    @Autowired
    GlobalHipChatConfigActions globalHipChatConfigActions;

    @Override
    public GlobalHipChatRepository getGlobalEntityRepository() {
        return globalHipChatRepository;
    }

    @Override
    public MockHipChatGlobalEntity getGlobalEntityMockUtil() {
        return new MockHipChatGlobalEntity();
    }

    @Override
    public MockHipChatGlobalRestModel getGlobalRestModelMockUtil() {
        return new MockHipChatGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/hipchat";
    }

    @Override
    public ConfigController<GlobalHipChatConfigRestModel> getController() {
        return new GlobalHipChatConfigController(globalHipChatConfigActions, new ObjectTransformer());
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final GlobalHipChatConfigRestModel globalHipChatConfigRestModel = new GlobalHipChatConfigRestModel(null, testProperties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), true);
        request.content(globalHipChatConfigRestModel.toString());
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
