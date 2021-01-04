package com.synopsys.integration.alert.web.api.metadata;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

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
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getDescriptorsWithNameOnlyTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        String componentName = DescriptorType.CHANNEL.name().toLowerCase() + "_2";
        request.requestAttr("name", componentName);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void getDescriptorsWithTypeOnlyTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.requestAttr("type", DescriptorType.CHANNEL.name());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getDescriptorsWithContextOnlyTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.requestAttr("context", ConfigContextEnum.GLOBAL.name());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void getDescriptorsWithNameAndContextTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        String descriptorName = DescriptorType.CHANNEL.name().toLowerCase() + "_4";
        request.requestAttr("name", descriptorName);
        request.requestAttr("context", ConfigContextEnum.GLOBAL.name());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getDescriptorsWithTypeAndContextTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.requestAttr("type", DescriptorType.CHANNEL.name());
        request.requestAttr("context", ConfigContextEnum.GLOBAL.name());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getDescriptorsWithAllParametersTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(DescriptorController.BASE_PATH))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        String descriptorName = DescriptorType.CHANNEL.name().toLowerCase() + "_4";
        request.requestAttr("name", descriptorName);
        request.requestAttr("type", DescriptorType.COMPONENT.name());
        request.requestAttr("context", ConfigContextEnum.GLOBAL.name());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
