package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.DatabaseConfiguredFieldTest;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

public class ConfigControllerTestIT extends DatabaseConfiguredFieldTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private final TypeToken componentListType = new TypeToken<ResponseEntity<String>>() {};
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSaveConfig() throws Exception {
        final String getUrl = ConfigController.CONFIGURATION_PATH;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));

        final String descriptorName = HipChatChannel.COMPONENT_NAME;
        final String context = ConfigContextEnum.GLOBAL.name();

        final String color = "blue";
        final String frequency = FrequencyType.DAILY.name();
        final String url = "url";

        final Map<String, FieldValueModel> fields = Map.of(HipChatDescriptor.KEY_COLOR, new FieldValueModel(List.of(color), true), CommonDistributionUIConfig.KEY_FREQUENCY,
            new FieldValueModel(List.of(frequency), true), BlackDuckDescriptor.KEY_BLACKDUCK_URL, new FieldValueModel(List.of(url), true));
        final FieldModel fieldModel = new FieldModel(descriptorName, context, fields);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        assertNotNull(response);

        final ResponseEntity<String> responseEntity = gson.fromJson(response, componentListType.getType());
        final JsonObject jsonObject = gson.fromJson(responseEntity.getBody(), JsonObject.class);
        final Long id = jsonObject.get("id").getAsLong();

        final Optional<ConfigurationModel> configurationModelOptional = getConfigurationAccessor().getConfigurationById(id);
        assertTrue(configurationModelOptional.isPresent());

        final ConfigurationModel configurationModel = configurationModelOptional.get();
        final Optional<ConfigurationFieldModel> colorField = configurationModel.getField(HipChatDescriptor.KEY_COLOR);
        final Optional<ConfigurationFieldModel> frequencyField = configurationModel.getField(CommonDistributionUIConfig.KEY_FREQUENCY);
        final Optional<ConfigurationFieldModel> urlField = configurationModel.getField(BlackDuckDescriptor.KEY_BLACKDUCK_URL);

        assertTrue(colorField.isPresent());
        assertTrue(frequencyField.isPresent());
        assertTrue(urlField.isPresent());

        assertEquals(color, colorField.get().getFieldValue().orElse(""));
        assertEquals(frequency, frequencyField.get().getFieldValue().orElse(""));
        assertEquals(url, urlField.get().getFieldValue().orElse(""));
    }
}
