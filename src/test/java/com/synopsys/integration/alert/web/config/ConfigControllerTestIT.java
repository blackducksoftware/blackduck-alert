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
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.DatabaseConfiguredFieldTest;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

public class ConfigControllerTestIT extends DatabaseConfiguredFieldTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    HipChatDescriptor hipChatDescriptor;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    public void testUpdateConfig() throws Exception {
//        registerDescriptor(hipChatDescriptor);
//        final String getUrl = ConfigController.CONFIGURATION_PATH;
//        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(getUrl)
//                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
//                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
//
//        final FieldModel fieldModel = createTestFieldModel();
//
//        request.content(gson.toJson(fieldModel));
//        request.contentType(contentType);
//
//        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
//        final String response = mvcResult.getResponse().getContentAsString();
//
//        assertNotNull(response);
//
//        final Map<String, Object> responseEntity = gson.fromJson(response, Map.class);
//        final String stringId = responseEntity.get("id").toString();
//        final Long id = (long) Double.parseDouble(stringId);
//
//        final Optional<ConfigurationModel> configurationModelOptional = getConfigurationAccessor().getConfigurationById(id);
//        assertTrue(configurationModelOptional.isPresent());
//
//        final ConfigurationModel configurationModel = configurationModelOptional.get();
//        final Optional<ConfigurationFieldModel> roomIdField = configurationModel.getField(HipChatDescriptor.KEY_ROOM_ID);
//        final Optional<ConfigurationFieldModel> frequencyField = configurationModel.getField(CommonDistributionUIConfig.KEY_FREQUENCY);
//        final Optional<ConfigurationFieldModel> filterByProjectField = configurationModel.getField(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT);
//
//        assertTrue(roomIdField.isPresent());
//        assertTrue(frequencyField.isPresent());
//        assertTrue(filterByProjectField.isPresent());
//
//        assertEquals("123", roomIdField.get().getFieldValue().orElse(""));
//        assertEquals(FrequencyType.DAILY.name(), frequencyField.get().getFieldValue().orElse(""));
//        assertEquals("false", filterByProjectField.get().getFieldValue().orElse(""));
//
//        unregisterDescriptor(hipChatDescriptor);
//    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSaveConfig() throws Exception {
        registerDescriptor(hipChatDescriptor);
        final String getUrl = ConfigController.CONFIGURATION_PATH + "?context=DISTRIBUTION";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final FieldModel fieldModel = createTestFieldModel();

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        assertNotNull(response);

        final Map<String, Object> responseEntity = gson.fromJson(response, Map.class);
        final String stringId = responseEntity.get("id").toString();
        final Long id = (long) Double.parseDouble(stringId);

        final Optional<ConfigurationModel> configurationModelOptional = getConfigurationAccessor().getConfigurationById(id);
        assertTrue(configurationModelOptional.isPresent());

        final ConfigurationModel configurationModel = configurationModelOptional.get();
        final Optional<ConfigurationFieldModel> roomIdField = configurationModel.getField(HipChatDescriptor.KEY_ROOM_ID);
        final Optional<ConfigurationFieldModel> frequencyField = configurationModel.getField(CommonDistributionUIConfig.KEY_FREQUENCY);
        final Optional<ConfigurationFieldModel> filterByProjectField = configurationModel.getField(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT);

        assertTrue(roomIdField.isPresent());
        assertTrue(frequencyField.isPresent());
        assertTrue(filterByProjectField.isPresent());

        assertEquals("123", roomIdField.get().getFieldValue().orElse(""));
        assertEquals(FrequencyType.DAILY.name(), frequencyField.get().getFieldValue().orElse(""));
        assertEquals("false", filterByProjectField.get().getFieldValue().orElse(""));

        unregisterDescriptor(hipChatDescriptor);
    }

    public FieldModel createTestFieldModel() {
        final String descriptorName = HipChatChannel.COMPONENT_NAME;
        final String context = ConfigContextEnum.DISTRIBUTION.name();

        final FieldValueModel roomId = new FieldValueModel(List.of("123"), true);
        final FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        final FieldValueModel name = new FieldValueModel(List.of("name"), true);
        final FieldValueModel provider = new FieldValueModel(List.of(BlackDuckProvider.COMPONENT_NAME), true);
        final FieldValueModel channel = new FieldValueModel(List.of("channel"), true);
        final FieldValueModel notificationType = new FieldValueModel(List.of("vulnerability"), true);
        final FieldValueModel formatType = new FieldValueModel(List.of(FormatType.DEFAULT.name()), true);
        final FieldValueModel filterByProject = new FieldValueModel(List.of("false"), true);

        final Map<String, FieldValueModel> fields = Map.of(HipChatDescriptor.KEY_ROOM_ID, roomId, CommonDistributionUIConfig.KEY_NAME, name, CommonDistributionUIConfig.KEY_PROVIDER_NAME, provider,
            CommonDistributionUIConfig.KEY_CHANNEL_NAME, channel, CommonDistributionUIConfig.KEY_FREQUENCY, frequency, ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationType, ProviderDistributionUIConfig.KEY_FORMAT_TYPE,
            formatType, BlackDuckDescriptor.KEY_FILTER_BY_PROJECT, filterByProject);
        return new FieldModel(descriptorName, context, fields);
    }
}
