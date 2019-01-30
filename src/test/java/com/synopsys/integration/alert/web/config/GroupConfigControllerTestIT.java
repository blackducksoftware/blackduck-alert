package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.DatabaseConfiguredFieldTest;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.JobFieldModel;

public class GroupConfigControllerTestIT extends DatabaseConfiguredFieldTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    @Autowired
    HipChatDescriptor hipChatDescriptor;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;

    private String url = JobConfigController.JOB_CONFIGURATION_PATH;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(HipChatChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "?context=" + ConfigContextEnum.DISTRIBUTION.name() + "&descriptorName=" + HipChatChannel.COMPONENT_NAME;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        final TypeToken fieldModelListType = new TypeToken<List<JobFieldModel>>() {};
        final List<JobFieldModel> fieldModels = gson.fromJson(response, fieldModelListType.getType());

        assertNotNull(fieldModels);
        assertFalse(fieldModels.isEmpty());
        assertTrue(fieldModels.stream()
                       .filter(fieldModel -> fieldModel.getJobId().equals(configId))
                       .findFirst()
                       .isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfigById() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(HipChatChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "/" + configId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        final FieldModel fieldModel = gson.fromJson(response, FieldModel.class);
        assertNotNull(fieldModel);
        assertEquals(configId, fieldModel.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(HipChatChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "/" + configId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());

        final UUID id = UUID.fromString(configId);
        final Optional<ConfigurationJobModel> configuration = getConfigurationAccessor().getJobById(id);

        assertTrue(configuration.isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateConfig() throws Exception {
        final JobFieldModel fieldModel = createTestJobFieldModel();
        Map<String, Collection<String>> fieldValueModels = new HashMap<>();
        for (FieldModel newFieldModel : fieldModel.getFieldModels()) {
            fieldValueModels.putAll(newFieldModel.getKeyToValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValues())));
        }
        final ConfigurationJobModel emptyConfigurationModel = addJob(HipChatChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, fieldValueModels);
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());
        final String urlPath = url + "/" + configId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        fieldModel.setJobId(configId);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSaveConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final JobFieldModel fieldModel = createTestJobFieldModel();

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidateConfig() throws Exception {
        final String urlPath = url + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final JobFieldModel fieldModel = createTestJobFieldModel();

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    // FIXME Will need to add all configurations to properly run a test check for hipchat.
    //    @Test
    //    @WithMockUser(roles = "ADMIN")
    //    public void testTestConfig() throws Exception {
    //        registerDescriptor(hipChatDescriptor);
    //        final String urlPath = url + "/test";
    //        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
    //                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
    //                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
    //
    //        final FieldModel fieldModel = createTestFieldModel();
    //
    //        request.content(gson.toJson(fieldModel));
    //        request.contentType(contentType);
    //
    //        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    //        unregisterDescriptor(hipChatDescriptor);
    //    }

    private JobFieldModel createTestJobFieldModel() {
        final String descriptorName = HipChatChannel.COMPONENT_NAME;
        final String context = ConfigContextEnum.DISTRIBUTION.name();

        final FieldValueModel roomId = new FieldValueModel(List.of("123"), true);
        final FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        final FieldValueModel name = new FieldValueModel(List.of("name"), true);
        final FieldValueModel provider = new FieldValueModel(List.of(BlackDuckProvider.COMPONENT_NAME), true);
        final FieldValueModel channel = new FieldValueModel(List.of("channel"), true);

        final Map<String, FieldValueModel> fields = Map.of(HipChatDescriptor.KEY_ROOM_ID, roomId, ChannelDistributionUIConfig.KEY_NAME, name, ChannelDistributionUIConfig.KEY_PROVIDER_NAME, provider,
            ChannelDistributionUIConfig.KEY_CHANNEL_NAME, channel, ChannelDistributionUIConfig.KEY_FREQUENCY, frequency);
        final FieldModel fieldModel = new FieldModel(descriptorName, context, fields);

        String bdDescriptorName = BlackDuckProvider.COMPONENT_NAME;
        String bdContext = ConfigContextEnum.DISTRIBUTION.name();

        final FieldValueModel notificationType = new FieldValueModel(List.of("vulnerability"), true);
        final FieldValueModel formatType = new FieldValueModel(List.of(FormatType.DEFAULT.name()), true);
        final FieldValueModel filterByProject = new FieldValueModel(List.of("false"), true);
        FieldValueModel projectNames = new FieldValueModel(List.of("project"), true);

        Map<String, FieldValueModel> bdFields = Map.of(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationType, ProviderDistributionUIConfig.KEY_FORMAT_TYPE,
            formatType, BlackDuckDescriptor.KEY_FILTER_BY_PROJECT, filterByProject, BlackDuckDescriptor.KEY_CONFIGURED_PROJECT, projectNames);
        FieldModel bdFieldModel = new FieldModel(bdDescriptorName, bdContext, bdFields);

        return new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel, bdFieldModel));
    }

    private void checkResponse(final String response) throws AlertDatabaseConstraintException {
        assertNotNull(response);

        final Map<String, Object> responseEntity = gson.fromJson(response, Map.class);
        final String stringId = responseEntity.get("id").toString();
        final UUID id = UUID.fromString(stringId);

        final Optional<ConfigurationJobModel> configurationModelOptional = getConfigurationAccessor().getJobById(id);
        assertTrue(configurationModelOptional.isPresent());

        Optional<ConfigurationFieldModel> roomIdField = Optional.empty();
        Optional<ConfigurationFieldModel> frequencyField = Optional.empty();
        Optional<ConfigurationFieldModel> filterByProjectField = Optional.empty();

        final ConfigurationJobModel configurationJobModel = configurationModelOptional.get();
        for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            if (roomIdField.isEmpty()) {
                roomIdField = configurationModel.getField(HipChatDescriptor.KEY_ROOM_ID);
            }
            if (frequencyField.isEmpty()) {
                frequencyField = configurationModel.getField(ChannelDistributionUIConfig.KEY_FREQUENCY);
            }
            if (filterByProjectField.isEmpty()) {
                filterByProjectField = configurationModel.getField(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT);
            }
        }

        assertTrue(roomIdField.isPresent());
        assertTrue(frequencyField.isPresent());
        assertTrue(filterByProjectField.isPresent());

        assertEquals("123", roomIdField.get().getFieldValue().orElse(""));
        assertEquals(FrequencyType.DAILY.name(), frequencyField.get().getFieldValue().orElse(""));
        assertEquals("false", filterByProjectField.get().getFieldValue().orElse(""));
    }
}
