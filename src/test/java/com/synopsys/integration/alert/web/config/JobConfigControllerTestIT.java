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

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DatabaseConfiguredFieldTest;

@Transactional
public class JobConfigControllerTestIT extends DatabaseConfiguredFieldTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String url = JobConfigController.JOB_CONFIGURATION_PATH;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
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
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfig() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(SlackChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "?context=" + ConfigContextEnum.DISTRIBUTION.name() + "&descriptorName=" + SlackChannel.COMPONENT_NAME;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
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
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfigById() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(SlackChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "/" + configId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        final ConfigurationJobModel fieldModel = gson.fromJson(response, ConfigurationJobModel.class);
        assertNotNull(fieldModel);
        assertEquals(configId, fieldModel.getJobId().toString());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testDeleteConfig() throws Exception {
        final ConfigurationJobModel emptyConfigurationModel = addJob(SlackChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, Map.of());
        final String jobId = String.valueOf(emptyConfigurationModel.getJobId());

        final String urlPath = url + "/" + jobId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
        descriptorConfigRepository.flush();

        final UUID id = UUID.fromString(jobId);
        final Optional<ConfigurationJobModel> configuration = getConfigurationAccessor().getJobById(id);

        assertTrue(configuration.isEmpty(), "Expected the job to have been deleted");
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testUpdateConfig() throws Exception {
        final JobFieldModel fieldModel = createTestJobFieldModel("1", "2");
        final Map<String, Collection<String>> fieldValueModels = new HashMap<>();
        for (final FieldModel newFieldModel : fieldModel.getFieldModels()) {
            fieldValueModels.putAll(newFieldModel.getKeyToValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValues())));
        }
        final ConfigurationJobModel emptyConfigurationModel = addJob(SlackChannel.COMPONENT_NAME, BlackDuckProvider.COMPONENT_NAME, fieldValueModels);
        final String configId = String.valueOf(emptyConfigurationModel.getJobId());
        final String urlPath = url + "/" + configId;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        fieldModel.setJobId(configId);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();
        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testSaveConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final JobFieldModel fieldModel = createTestJobFieldModel(null, null);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        final MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        final String response = mvcResult.getResponse().getContentAsString();

        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testValidateConfig() throws Exception {
        final String urlPath = url + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());

        final JobFieldModel fieldModel = createTestJobFieldModel(null, null);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private JobFieldModel createTestJobFieldModel(String channelId, String providerId) {
        final String descriptorName = SlackChannel.COMPONENT_NAME;
        final String context = ConfigContextEnum.DISTRIBUTION.name();

        final FieldValueModel slackChannelName = new FieldValueModel(List.of("channelName"), true);
        final FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        final FieldValueModel name = new FieldValueModel(List.of("name"), true);
        final FieldValueModel provider = new FieldValueModel(List.of(BlackDuckProvider.COMPONENT_NAME), true);
        final FieldValueModel channel = new FieldValueModel(List.of("channel_slack"), true);
        final FieldValueModel webhook = new FieldValueModel(List.of("slack_webhook_url"), true);

        final Map<String, FieldValueModel> fields = Map.of(SlackDescriptor.KEY_CHANNEL_NAME, slackChannelName,
            SlackDescriptor.KEY_WEBHOOK, webhook,
            ChannelDistributionUIConfig.KEY_NAME, name,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, provider,
            ChannelDistributionUIConfig.KEY_CHANNEL_NAME, channel,
            ChannelDistributionUIConfig.KEY_FREQUENCY, frequency);
        final FieldModel fieldModel = new FieldModel(descriptorName, context, fields);
        if (StringUtils.isNotBlank(channelId)) {
            fieldModel.setId(channelId);
        }

        final String bdDescriptorName = BlackDuckProvider.COMPONENT_NAME;
        final String bdContext = ConfigContextEnum.DISTRIBUTION.name();

        final FieldValueModel notificationType = new FieldValueModel(List.of("vulnerability"), true);
        final FieldValueModel formatType = new FieldValueModel(List.of(FormatType.DEFAULT.name()), true);
        final FieldValueModel filterByProject = new FieldValueModel(List.of("false"), true);
        final FieldValueModel projectNames = new FieldValueModel(List.of("project"), true);

        final Map<String, FieldValueModel> bdFields = Map.of(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationType, ProviderDistributionUIConfig.KEY_FORMAT_TYPE,
            formatType, ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, filterByProject, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, projectNames);
        final FieldModel bdFieldModel = new FieldModel(bdDescriptorName, bdContext, bdFields);
        if (StringUtils.isNotBlank(providerId)) {
            bdFieldModel.setId(providerId);
        }

        return new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel, bdFieldModel));
    }

    private void checkResponse(final String response) throws AlertDatabaseConstraintException {
        assertNotNull(response);

        final Map<String, Object> responseEntity = gson.fromJson(response, Map.class);
        final String stringId = responseEntity.get("id").toString();
        final UUID id = UUID.fromString(stringId);

        final Optional<ConfigurationJobModel> configurationModelOptional = getConfigurationAccessor().getJobById(id);
        assertTrue(configurationModelOptional.isPresent());

        Optional<ConfigurationFieldModel> slackChannelNameField = Optional.empty();
        Optional<ConfigurationFieldModel> frequencyField = Optional.empty();
        Optional<ConfigurationFieldModel> filterByProjectField = Optional.empty();

        final ConfigurationJobModel configurationJobModel = configurationModelOptional.get();

        for (final ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
            if (slackChannelNameField.isEmpty()) {
                slackChannelNameField = configurationModel.getField(SlackDescriptor.KEY_CHANNEL_NAME);
            }
            if (frequencyField.isEmpty()) {
                frequencyField = configurationModel.getField(ChannelDistributionUIConfig.KEY_FREQUENCY);
            }
            if (filterByProjectField.isEmpty()) {
                filterByProjectField = configurationModel.getField(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT);
            }
        }

        assertTrue(slackChannelNameField.isPresent());
        assertTrue(frequencyField.isPresent());
        assertTrue(filterByProjectField.isPresent());

        assertEquals("channelName", slackChannelNameField.get().getFieldValue().orElse(""));
        assertEquals(FrequencyType.DAILY.name(), frequencyField.get().getFieldValue().orElse(""));
        assertEquals("false", filterByProjectField.get().getFieldValue().orElse(""));
    }

}
