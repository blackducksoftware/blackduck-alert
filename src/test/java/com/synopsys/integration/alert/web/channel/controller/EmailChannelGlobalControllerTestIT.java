package com.synopsys.integration.alert.web.channel.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;
import com.synopsys.integration.alert.web.model.Config;

public class EmailChannelGlobalControllerTestIT extends GlobalControllerTest {

    @Autowired
    private EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor;

    @Override
    public EmailGlobalRepositoryAccessor getGlobalRepositoryAccessor() {
        return emailGlobalRepositoryAccessor;
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/channel/global/channel_email";
    }

    @Override
    public DatabaseEntity getGlobalEntity() {
        final MockEmailGlobalEntity mockGlobalEntity = new MockEmailGlobalEntity();
        return mockGlobalEntity.createGlobalEntity();
    }

    @Override
    public Config getGlobalConfig() {
        final MockEmailGlobalRestModel mockGlobalConfig = new MockEmailGlobalRestModel();
        mockGlobalConfig.setMailSmtpHost(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        mockGlobalConfig.setMailSmtpFrom(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        mockGlobalConfig.setMailSmtpUser(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        mockGlobalConfig.setMailSmtpPassword(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        mockGlobalConfig.setMailSmtpEhlo(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO)));
        mockGlobalConfig.setMailSmtpAuth(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH)));
        mockGlobalConfig.setMailSmtpPort(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));
        return mockGlobalConfig.createGlobalRestModel();
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        config.setId(String.valueOf(entity.getId()));
        request.content(gson.toJson(config));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Override
    public String getTestDestination() {
        return testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);
    }

}
