/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details.processor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class JobDetailsExtractor {
    public DistributionJobModel convertToJobModel(
        UUID jobId,
        Map<String, ConfigurationFieldModel> configuredFieldsMap,
        OffsetDateTime createdAt,
        @Nullable OffsetDateTime lastUpdated,
        List<BlackDuckProjectDetailsModel> projectFilterDetails
    ) {
        String channelDescriptorName = extractFieldValueOrEmptyString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, configuredFieldsMap);
        DistributionJobModelBuilder builder = DistributionJobModel.builder()
                                                  .jobId(jobId)
                                                  .enabled(extractFieldValue(ChannelDistributionUIConfig.KEY_ENABLED, configuredFieldsMap).map(Boolean::valueOf).orElse(true))
                                                  .name(extractFieldValueOrEmptyString(ChannelDistributionUIConfig.KEY_NAME, configuredFieldsMap))
                                                  .distributionFrequency(extractFieldValueOrEmptyString(ChannelDistributionUIConfig.KEY_FREQUENCY, configuredFieldsMap))
                                                  .processingType(extractFieldValueOrEmptyString(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, configuredFieldsMap))
                                                  .channelDescriptorName(channelDescriptorName)
                                                  .createdAt(createdAt)
                                                  .lastUpdated(lastUpdated)

                                                  .blackDuckGlobalConfigId(extractFieldValue(ProviderDistributionUIConfig.KEY_COMMON_CONFIG_ID, configuredFieldsMap).map(Long::valueOf).orElse(-1L))
                                                  .filterByProject(extractFieldValue(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, configuredFieldsMap).map(Boolean::valueOf).orElse(false))
                                                  .projectNamePattern(extractFieldValue(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, configuredFieldsMap).orElse(null))
                                                  .notificationTypes(extractFieldValues(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, configuredFieldsMap))
                                                  .policyFilterPolicyNames(extractFieldValues("blackduck.policy.notification.filter", configuredFieldsMap))
                                                  .vulnerabilityFilterSeverityNames(extractFieldValues("blackduck.vulnerability.notification.filter", configuredFieldsMap))
                                                  .projectFilterDetails(projectFilterDetails);

        DistributionJobDetailsModel distributionJobDetailsModel = convertToChannelJobDetails(jobId, configuredFieldsMap);
        builder.distributionJobDetails(distributionJobDetailsModel);

        return builder.build();
    }

    protected abstract DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap);

    protected String extractFieldValueOrEmptyString(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return extractFieldValue(fieldKey, configuredFieldsMap).orElse("");
    }

    protected Optional<String> extractFieldValue(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return extractFieldValues(fieldKey, configuredFieldsMap)
                   .stream()
                   .findAny();
    }

    protected List<String> extractFieldValues(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        ConfigurationFieldModel fieldModel = configuredFieldsMap.get(fieldKey);
        if (null != fieldModel) {
            return new ArrayList<>(fieldModel.getFieldValues());
        }
        return List.of();
    }

}
