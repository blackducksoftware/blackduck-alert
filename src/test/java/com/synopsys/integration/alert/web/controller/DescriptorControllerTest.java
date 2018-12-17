package com.synopsys.integration.alert.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public class DescriptorControllerTest extends AlertIntegrationTest {

    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    protected MockMvc mockMvc;
    protected Gson gson;
    protected TypeToken componentListType = new TypeToken<List<UIComponent>>() {};

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private DescriptorMap descriptorMap;

    @Before
    public void setup() {
        gson = new Gson();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyNameTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorName=channel_email";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        final List<UIComponent> expected = descriptorMap.getDescriptor("channel_email").getAllUIConfigs()
                                               .stream()
                                               .map(uiConfig -> uiConfig.generateUIComponent()).collect(Collectors.toList());
        assertEquals(expected.size(), componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyNameAndDescriptorTypeTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorName=channel_email&descriptorType=CHANNEL_GLOBAL_CONFIG";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        assertEquals(1, componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyNameNotFoundTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorName=bad_name";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        assertEquals(0, componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyNameAndDescriptorTypeNotFoundTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorName=channel_email&descriptorType=bad_type";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        assertEquals(0, componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyDescriptorTypeTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorType=CHANNEL_GLOBAL_CONFIG";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        final List<UIComponent> expected = descriptorMap.getUIComponents(ConfigContextEnum.GLOBAL);
        assertEquals(expected.size(), componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getbyDescriptorTypeNotFoundTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorType=bad_type";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        assertEquals(0, componentList.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void badNameAndDescriptorTypeTest() throws Exception {
        final String getUrl = UIComponentController.DESCRIPTOR_PATH + "?descriptorName=bad_name&descriptorType=bad_type";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        final MvcResult result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        final String body = result.getResponse().getContentAsString();
        assertNotNull(body);

        final List<UIComponent> componentList = gson.fromJson(body, componentListType.getType());
        assertNotNull(componentList);
        assertEquals(0, componentList.size());
    }
}
