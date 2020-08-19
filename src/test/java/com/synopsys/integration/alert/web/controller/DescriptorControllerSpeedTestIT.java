package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.metadata.DescriptorController;
import com.synopsys.integration.alert.web.api.metadata.MetadataController;

public class DescriptorControllerSpeedTestIT extends AlertIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Collection<Descriptor> descriptors;

    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testDescriptorEndpoint() throws Exception {
        String urlPath = MetadataController.METADATA_BASE_PATH + DescriptorController.DESCRIPTORS_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        long startTime = System.nanoTime();
        MvcResult mvcResult = mockMvc.perform(request).andReturn();
        long endTime = System.nanoTime();
        long totalRunTime = endTime - startTime;
        long timeInMillis = TimeUnit.NANOSECONDS.toMillis(totalRunTime);

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonArray descriptorMetadata = gson.fromJson(responseContent, new TypeToken<JsonArray>() {}.getType());

        assertTrue(descriptorMetadata.size() >= descriptors.size());
        long expectedMaxTime = 500;
        assertTrue(timeInMillis < expectedMaxTime, "Total runtime was: " + timeInMillis + "ms and should be below: " + expectedMaxTime + "ms.");
    }

}
