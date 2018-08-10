package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.mock.MockGlobalEntityUtil;
import com.synopsys.integration.alert.mock.MockGlobalRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;

public class EmailChannelGlobalControllerTestIT extends GlobalControllerTest<EmailGlobalConfigEntity, EmailGlobalConfig, EmailGlobalRepository> {

    @Autowired
    private EmailGlobalRepository emailGlobalRepository;

    @Override
    public EmailGlobalRepository getGlobalEntityRepository() {
        return emailGlobalRepository;
    }

    @Override
    public MockGlobalEntityUtil<EmailGlobalConfigEntity> getGlobalEntityMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public MockGlobalRestModelUtil<EmailGlobalConfig> getGlobalRestModelMockUtil() {
        return new MockEmailGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/global/channel_email";
    }

    @Override
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

}
