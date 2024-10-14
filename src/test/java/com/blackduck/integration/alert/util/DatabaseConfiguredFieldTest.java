/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@AlertIntegrationTest
public abstract class DatabaseConfiguredFieldTest {
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    public DistributionJobModel createAndSaveMockDistributionJob(Long blackDuckGlobalConfigId) {
        DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(blackDuckGlobalConfigId);
        return addDistributionJob(jobRequestModel);
    }

    public DistributionJobModel addDistributionJob(DistributionJobRequestModel jobRequestModel) {
        return jobAccessor.createJob(jobRequestModel);
    }

    public ConfigurationModel addGlobalConfiguration(DescriptorKey descriptorKey, Map<String, Collection<String>> fieldsValues) {
        Set<ConfigurationFieldModel> fieldModels = fieldsValues
            .entrySet()
            .stream()
            .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
            .collect(Collectors.toSet());

        return configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.GLOBAL, fieldModels);
    }

    public ConfigurationFieldModel createConfigurationFieldModel(String key, Collection<String> values) {
        ConfigurationFieldModel configurationFieldModel = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY.equals(key) ? ConfigurationFieldModel.createSensitive(key) : ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

    public ConfigurationModelConfigurationAccessor getConfigurationAccessor() {
        return configurationModelConfigurationAccessor;
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(Long blackDuckGlobalConfigId) {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "channel_webhook", getClass().getSimpleName());
        return new DistributionJobRequestModel(
            true,
            getClass().getSimpleName() + " Test Job",
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
            blackDuckGlobalConfigId,
            false,
            null,
            null,
            List.of("notificationType"),
            List.of(),
            List.of(),
            List.of(),
            details
        );
    }

}
