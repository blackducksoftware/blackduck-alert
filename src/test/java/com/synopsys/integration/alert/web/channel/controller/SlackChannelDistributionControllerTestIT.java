package com.synopsys.integration.alert.web.channel.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.controller.ControllerTest;

public class SlackChannelDistributionControllerTestIT extends ControllerTest<SlackDistributionConfigEntity, SlackDistributionConfig, SlackDistributionRepository> {

    @Autowired
    private SlackDistributionRepository slackDistributionRepository;

    @Override
    public SlackDistributionRepository getEntityRepository() {
        return slackDistributionRepository;
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getEntityMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockRestModelUtil<SlackDistributionConfig> getRestModelMockUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public String getDescriptorName() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Test
    @Override
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.save(distributionMockUtil.createEntity());
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        restModel.setDistributionConfigId(String.valueOf(savedEntity.getId()));
        restModel.setId(String.valueOf(commonEntity.getId()));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

}
