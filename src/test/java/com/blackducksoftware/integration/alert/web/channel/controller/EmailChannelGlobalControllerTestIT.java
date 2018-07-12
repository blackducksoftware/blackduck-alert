package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailRepository;
import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;
import com.blackducksoftware.integration.alert.mock.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.web.controller.GlobalControllerTest;

public class EmailChannelGlobalControllerTestIT extends GlobalControllerTest<GlobalEmailConfigEntity, GlobalEmailConfigRestModel, GlobalEmailRepository> {

    @Autowired
    private GlobalEmailRepository globalEmailRepository;

    @Override
    public GlobalEmailRepository getGlobalEntityRepository() {
        return globalEmailRepository;
    }

    @Override
    public MockGlobalEntityUtil<GlobalEmailConfigEntity> getGlobalEntityMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public MockGlobalRestModelUtil<GlobalEmailConfigRestModel> getGlobalRestModelMockUtil() {
        return new MockEmailGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/global/channel_email";
    }

    @Override
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

}
