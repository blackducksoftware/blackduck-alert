/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

public abstract class JobDetailsProcessor {

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

                                                  .blackDuckGlobalConfigId(extractFieldValue("provider.common.config.id", configuredFieldsMap).map(Long::valueOf).orElse(-1L))
                                                  .filterByProject(extractFieldValue("channel.common.filter.by.project", configuredFieldsMap).map(Boolean::valueOf).orElse(false))
                                                  .projectNamePattern(extractFieldValue("channel.common.project.name.pattern", configuredFieldsMap).orElse(null))
                                                  .notificationTypes(extractFieldValues("provider.distribution.notification.types", configuredFieldsMap))
                                                  .policyFilterPolicyNames(extractFieldValues("blackduck.policy.notification.filter", configuredFieldsMap))
                                                  .vulnerabilityFilterSeverityNames(extractFieldValues("blackduck.vulnerability.notification.filter", configuredFieldsMap))
                                                  .projectFilterDetails(projectFilterDetails);

        DistributionJobDetailsModel distributionJobDetailsModel = convertToChannelJobDetails(configuredFieldsMap);
        builder.distributionJobDetails(distributionJobDetailsModel);

        return builder.build();
    }

    protected abstract DistributionJobDetailsModel convertToChannelJobDetails(Map<String, ConfigurationFieldModel> configuredFieldsMap);

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
