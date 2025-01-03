/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.job;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;
import com.blackduck.integration.alert.common.rest.model.JobIdsRequestModel;
import com.blackduck.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.util.AlertIntegrationTestConstants;
import com.blackduck.integration.alert.util.DatabaseConfiguredFieldTest;
import com.google.gson.Gson;

public class JobConfigControllerTestIT extends DatabaseConfiguredFieldTest {
    private static final String DEFAULT_BLACK_DUCK_CONFIG = "Default Black Duck Config";
    private static final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private static final String REQUEST_URL = JobConfigController.JOB_CONFIGURATION_PATH;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;
    @Autowired
    private Gson gson;
    @Autowired
    private JobAccessor jobAccessor;

    private final TestProperties testProperties = new TestProperties();
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @AfterEach
    public void cleanUp() {
        jobAccessor.getPageOfJobs(0, 100)
            .getModels()
            .stream()
            .map(DistributionJobModel::getJobId)
            .forEach(jobAccessor::deleteJob);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetValidationResultsForJobs() throws Exception {
        final String urlPath = REQUEST_URL + "/validateJobsById";
        DistributionJobModel distributionJobModel = createAndSaveMockDistributionJob(-1L);
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of(distributionJobModel.getJobId()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(gson.toJson(jobIdsRequestModel))
            .contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testDescriptorCheck() throws Exception {
        final String urlPath = REQUEST_URL + "/descriptorCheck";
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(blackDuckProviderKey.getUniversalKey())
            .contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetPage() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        createAndSaveMockDistributionJob(-1L);

        String urlPath = REQUEST_URL + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetConfigById() throws Exception {
        DistributionJobModel distributionJobModel = createAndSaveMockDistributionJob(-1L);
        String configId = String.valueOf(distributionJobModel.getJobId());

        String urlPath = REQUEST_URL + "/" + configId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testDeleteConfig() throws Exception {
        DistributionJobModel distributionJobModel = createAndSaveMockDistributionJob(-1L);
        String jobId = String.valueOf(distributionJobModel.getJobId());
        addGlobalConfiguration(blackDuckProviderKey, Map.of(
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        String urlPath = REQUEST_URL + "/" + jobId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testUpdateConfig() throws Exception {
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
                ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
                BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of(testProperties.getBlackDuckURL()),
                BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of(testProperties.getBlackDuckAPIToken())
            )
        );

        JobFieldModel fieldModel = createTestJobFieldModel("1", "2", providerGlobalConfig);
        Map<String, Collection<String>> fieldValueModels = new HashMap<>();
        for (FieldModel newFieldModel : fieldModel.getFieldModels()) {
            fieldValueModels.putAll(newFieldModel.getKeyToValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValues())));
        }

        DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(providerGlobalConfig.getConfigurationId());
        DistributionJobModel distributionJobModel = addDistributionJob(jobRequestModel);

        String configId = String.valueOf(distributionJobModel.getJobId());
        String urlPath = REQUEST_URL + "/" + configId;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        fieldModel.setJobId(configId);

        request.content(gson.toJson(fieldModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testSaveConfig() throws Exception {
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
                ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
                BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of(testProperties.getBlackDuckURL()),
                BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of(testProperties.getBlackDuckAPIToken())
            )
        );
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null, providerGlobalConfig);

        request.content(gson.toJson(fieldModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testValidateConfig() throws Exception {
        final String urlPath = REQUEST_URL + "/validate";
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null, providerGlobalConfig);

        request.content(gson.toJson(fieldModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testTestConfig() throws Exception {
        final String urlPath = REQUEST_URL + "/test";
        ConfigurationModel providerGlobalConfig = addGlobalConfiguration(blackDuckProviderKey, Map.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, List.of(DEFAULT_BLACK_DUCK_CONFIG),
            BlackDuckDescriptor.KEY_BLACKDUCK_URL, List.of("BLACKDUCK_URL"),
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, List.of("BLACKDUCK_API")));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        JobFieldModel fieldModel = createTestJobFieldModel(null, null, providerGlobalConfig);

        request.content(gson.toJson(fieldModel));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(Long blackDuckGlobalConfigId) {
        SlackJobDetailsModel slackJobDetails = new SlackJobDetailsModel(null, "http://slack_webhook_url", null);
        return new DistributionJobRequestModel(
            true,
            "name",
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
            blackDuckGlobalConfigId,
            false,
            null,
            null,
            List.of("VULNERABILITY"),
            List.of(),
            List.of(),
            List.of(),
            slackJobDetails
        );
    }

    private JobFieldModel createTestJobFieldModel(String channelId, String providerId, ConfigurationModel providerGlobalConfig) {
        String descriptorName = ChannelKeys.SLACK.getUniversalKey();
        String context = ConfigContextEnum.DISTRIBUTION.name();

        FieldValueModel providerConfigField = new FieldValueModel(List.of(providerGlobalConfig.getConfigurationId().toString()), true);
        FieldValueModel slackChannelName = new FieldValueModel(List.of("channelName"), true);
        FieldValueModel frequency = new FieldValueModel(List.of(FrequencyType.DAILY.name()), true);
        FieldValueModel name = new FieldValueModel(List.of("name"), true);
        FieldValueModel provider = new FieldValueModel(List.of(blackDuckProviderKey.getUniversalKey()), true);
        FieldValueModel channel = new FieldValueModel(List.of("channel_slack"), true);
        FieldValueModel webhook = new FieldValueModel(List.of("http://slack_webhook_url"), true);

        Map<String, FieldValueModel> fields = Map.of(
            SlackDescriptor.KEY_WEBHOOK, webhook,
            ChannelDescriptor.KEY_NAME, name,
            ChannelDescriptor.KEY_PROVIDER_TYPE, provider,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, providerConfigField,
            ChannelDescriptor.KEY_CHANNEL_NAME, channel,
            ChannelDescriptor.KEY_FREQUENCY, frequency
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

        Map<String, FieldValueModel> bdFields = Map.of(ProviderDescriptor.KEY_NOTIFICATION_TYPES, notificationType,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, providerConfigField,
            ProviderDescriptor.KEY_PROCESSING_TYPE, formatType,
            ProviderDescriptor.KEY_FILTER_BY_PROJECT, filterByProject,
            ProviderDescriptor.KEY_CONFIGURED_PROJECT, projectNames
        );
        FieldModel bdFieldModel = new FieldModel(bdDescriptorName, bdContext, bdFields);
        if (StringUtils.isNotBlank(providerId)) {
            bdFieldModel.setId(providerId);
        }

        return new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel, bdFieldModel), List.of(new JobProviderProjectFieldModel("project", "href", false)));
    }

}
