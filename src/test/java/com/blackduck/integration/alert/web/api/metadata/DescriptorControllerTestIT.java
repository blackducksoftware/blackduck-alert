/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.alert.util.AlertIntegrationTestConstants;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorsResponseModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

@Transactional
@AlertIntegrationTest
public class DescriptorControllerTestIT {
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void getDescriptorsWithNoParametersTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        DescriptorsResponseModel descriptorsResponseModel = assertValidResponse(request);
        assertEquals(21, descriptorsResponseModel.getDescriptors().size());
    }

    @Test
    public void getDescriptorsWithNameOnlyTest() throws Exception {
        String msTeams = ChannelKeys.MS_TEAMS.getUniversalKey();
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("name", msTeams)
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        assertValidResponse(request);
    }

    @Test
    public void getDescriptorsWithTypeOnlyTest() throws Exception {
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("type", DescriptorType.CHANNEL.name())
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        assertValidResponse(request);
    }

    @Test
    public void getDescriptorsWithContextOnlyTest() throws Exception {
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("context", ConfigContextEnum.GLOBAL.name())
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        assertValidResponse(request);
    }

    @Test
    public void getDescriptorsWithNameAndContextTest() throws Exception {
        String msTeams = ChannelKeys.MS_TEAMS.getUniversalKey();
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("name", msTeams)
            .addParameter("context", ConfigContextEnum.GLOBAL.name())
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        DescriptorsResponseModel descriptorsResponseModel = assertValidResponse(request);
        assertEquals(1, descriptorsResponseModel.getDescriptors().size());
    }

    @Test
    public void getDescriptorsWithTypeAndContextTest() throws Exception {
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("type", DescriptorType.CHANNEL.name())
            .addParameter("context", ConfigContextEnum.GLOBAL.name())
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        assertValidResponse(request);
    }

    @Test
    public void getDescriptorsWithAllParametersTest() throws Exception {
        String msTeams = ChannelKeys.MS_TEAMS.getUniversalKey();
        URI descriptorPath = new URIBuilder(DescriptorController.BASE_PATH)
            .addParameter("name", msTeams)
            .addParameter("type", DescriptorType.CHANNEL.name())
            .addParameter("context", ConfigContextEnum.GLOBAL.name())
            .build();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(descriptorPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        DescriptorsResponseModel descriptorsResponseModel = assertValidResponse(request);
        assertEquals(1, descriptorsResponseModel.getDescriptors().size());
    }

    private DescriptorsResponseModel assertValidResponse(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String listOfDescriptorsJson = mvcResult.getResponse().getContentAsString();
        DescriptorsResponseModel descriptorsResponseModel = BlackDuckServicesFactory.createDefaultGson().fromJson(listOfDescriptorsJson, DescriptorsResponseModel.class);
        Set<DescriptorMetadata> setOfDescriptors = descriptorsResponseModel.getDescriptors();

        assertTrue(setOfDescriptors.size() > 0);

        return descriptorsResponseModel;
    }

}
