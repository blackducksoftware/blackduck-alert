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
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
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
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;
    @Autowired
    private SlackChannelKey slackChannelKey;
    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfig() throws Exception {
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), Map.of());
        String configId = String.valueOf(emptyConfigurationModel.getJobId());

        String urlPath = url + "?context=" + ConfigContextEnum.DISTRIBUTION.name() + "&descriptorName=" + slackChannelKey.getUniversalKey();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        TypeToken fieldModelListType = new TypeToken<List<JobFieldModel>>() {};
        List<JobFieldModel> fieldModels = gson.fromJson(response, fieldModelListType.getType());

        assertNotNull(fieldModels);
        assertFalse(fieldModels.isEmpty());
        assertTrue(fieldModels.stream()
                       .anyMatch(fieldModel -> fieldModel.getJobId().equals(configId)));
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfigById() throws Exception {
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), Map.of());
        String configId = String.valueOf(emptyConfigurationModel.getJobId());

        String urlPath = url + "/" + configId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        ConfigurationJobModel fieldModel = gson.fromJson(response, ConfigurationJobModel.class);
        assertNotNull(fieldModel);
        assertEquals(configId, fieldModel.getJobId().toString());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testDeleteConfig() throws Exception {
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), Map.of());
        String jobId = String.valueOf(emptyConfigurationModel.getJobId());
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        String urlPath = url + "/" + jobId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
        descriptorConfigRepository.flush();

        UUID id = UUID.fromString(jobId);
        Optional<ConfigurationJobModel> configuration = getConfigurationAccessor().getJobById(id);

        assertTrue(configuration.isEmpty(), "Expected the job to have been deleted");
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testUpdateConfig() throws Exception {
        JobFieldModel fieldModel = createTestJobFieldModel("1", "2");
        Map<String, Collection<String>> fieldValueModels = new HashMap<>();
        for (FieldModel newFieldModel : fieldModel.getFieldModels()) {
            fieldValueModels.putAll(newFieldModel.getKeyToValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValues())));
        }
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), fieldValueModels);
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        String configId = String.valueOf(emptyConfigurationModel.getJobId());
        String urlPath = url + "/" + configId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        fieldModel.setJobId(configId);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testSaveConfig() throws Exception {
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        checkResponse(response);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testValidateConfig() throws Exception {
        final String urlPath = url + "/validate";
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private JobFieldModel createTestJobFieldModel(String channelId, String providerId) {
        String descriptorName = slackChannelKey.getUniversalKey();
        String context = ConfigContextEnum.DISTRIBUTION.name();

        FieldValueModel providerConfig = new FieldValueModel(List.of("Default Black Duck Config"), true);
        FieldValueModel slackChannelName = new FieldValueModel(List.of("channelName"), true);
        FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        FieldValueModel name = new FieldValueModel(List.of("name"), true);
        FieldValueModel provider = new FieldValueModel(List.of(blackDuckProviderKey.getUniversalKey()), true);
        FieldValueModel channel = new FieldValueModel(List.of("channel_slack"), true);
        FieldValueModel webhook = new FieldValueModel(List.of("http://slack_webhook_url"), true);

        Map<String, FieldValueModel> fields = Map.of(SlackDescriptor.KEY_CHANNEL_NAME, slackChannelName,
            SlackDescriptor.KEY_WEBHOOK, webhook,
            ChannelDistributionUIConfig.KEY_NAME, name,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, provider,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, providerConfig,
            ChannelDistributionUIConfig.KEY_CHANNEL_NAME, channel,
            ChannelDistributionUIConfig.KEY_FREQUENCY, frequency
        );
        FieldModel fieldModel = new FieldModel(descriptorName, context, fields);
        if (StringUtils.isNotBlank(channelId)) {
            fieldModel.setId(channelId);
        }

        String bdDescriptorName = blackDuckProviderKey.getUniversalKey();
        String bdContext = ConfigContextEnum.DISTRIBUTION.name();

        FieldValueModel notificationType = new FieldValueModel(List.of("vulnerability"), true);
        FieldValueModel formatType = new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true);
        FieldValueModel filterByProject = new FieldValueModel(List.of("false"), true);
        FieldValueModel projectNames = new FieldValueModel(List.of("project"), true);

        Map<String, FieldValueModel> bdFields = Map.of(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationType,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, providerConfig,
            ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, formatType,
            ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, filterByProject,
            ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, projectNames
        );
        FieldModel bdFieldModel = new FieldModel(bdDescriptorName, bdContext, bdFields);
        if (StringUtils.isNotBlank(providerId)) {
            bdFieldModel.setId(providerId);
        }

        return new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel, bdFieldModel));
    }

    private void checkResponse(String response) throws AlertDatabaseConstraintException {
        assertNotNull(response);

        Map<String, Object> responseEntity = gson.fromJson(response, Map.class);
        String stringId = responseEntity.get("id").toString();
        UUID id = UUID.fromString(stringId);

        Optional<ConfigurationJobModel> configurationModelOptional = getConfigurationAccessor().getJobById(id);
        assertTrue(configurationModelOptional.isPresent());

        Optional<ConfigurationFieldModel> slackChannelNameField = Optional.empty();
        Optional<ConfigurationFieldModel> frequencyField = Optional.empty();
        Optional<ConfigurationFieldModel> filterByProjectField = Optional.empty();

        ConfigurationJobModel configurationJobModel = configurationModelOptional.get();

        for (ConfigurationModel configurationModel : configurationJobModel.getCopyOfConfigurations()) {
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
