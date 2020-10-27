package com.synopsys.integration.alert.web.api.job;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
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
    private static final String DEFAULT_BLACK_DUCK_CONFIG = "Default Black Duck Config";
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
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
    // FIXME replace with paged request equivalent
    @Ignore
    public void testGetConfig() throws Exception {
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), Map.of());
        String configId = String.valueOf(emptyConfigurationModel.getJobId());

        String urlPath = url + "?context=" + ConfigContextEnum.DISTRIBUTION.name() + "&descriptorName=" + slackChannelKey.getUniversalKey();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetPage() throws Exception {
        int pageNumber = 1;
        int pageSize = 10;
        addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), Map.of());

        String urlPath = url + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
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

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
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

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testUpdateConfig() throws Exception {
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));

        JobFieldModel fieldModel = createTestJobFieldModel("1", "2", providerGlobalConfig);
        Map<String, Collection<String>> fieldValueModels = new HashMap<>();
        for (FieldModel newFieldModel : fieldModel.getFieldModels()) {
            fieldValueModels.putAll(newFieldModel.getKeyToValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValues())));
        }
        ConfigurationJobModel emptyConfigurationModel = addJob(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey(), fieldValueModels);

        String configId = String.valueOf(emptyConfigurationModel.getJobId());
        String urlPath = url + "/" + configId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        fieldModel.setJobId(configId);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testSaveConfig() throws Exception {
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null, providerGlobalConfig);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testValidateConfig() throws Exception {
        final String urlPath = url + "/validate";
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null, providerGlobalConfig);

        request.content(gson.toJson(fieldModel));
        request.contentType(contentType);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private JobFieldModel createTestJobFieldModel(String channelId, String providerId, ConfigurationModel providerGlobalConfig) {
        String descriptorName = slackChannelKey.getUniversalKey();
        String context = ConfigContextEnum.DISTRIBUTION.name();

        FieldValueModel providerConfigField = new FieldValueModel(List.of(providerGlobalConfig.getConfigurationId().toString()), true);
        FieldValueModel slackChannelName = new FieldValueModel(List.of("channelName"), true);
        FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        FieldValueModel name = new FieldValueModel(List.of("name"), true);
        FieldValueModel provider = new FieldValueModel(List.of(blackDuckProviderKey.getUniversalKey()), true);
        FieldValueModel channel = new FieldValueModel(List.of("channel_slack"), true);
        FieldValueModel webhook = new FieldValueModel(List.of("http://slack_webhook_url"), true);

        Map<String, FieldValueModel> fields = Map.of(
            SlackDescriptor.KEY_CHANNEL_NAME, slackChannelName,
            SlackDescriptor.KEY_WEBHOOK, webhook,
            ChannelDistributionUIConfig.KEY_NAME, name,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, provider,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, providerConfigField,
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
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, providerConfigField,
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
}
