/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.descriptor.config.context;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionDescriptorActionApi extends DescriptorActionApi {
    private final Logger logger = LoggerFactory.getLogger(ChannelDistributionDescriptorActionApi.class);

    private final DistributionChannel distributionChannel;
    private final List<ProviderDescriptor> providerDescriptors;
    private final BaseConfigurationAccessor configurationAccessor;

    public ChannelDistributionDescriptorActionApi(final DistributionChannel distributionChannel, final List<ProviderDescriptor> providerDescriptors, final BaseConfigurationAccessor configurationAccessor) {
        this.distributionChannel = distributionChannel;
        this.providerDescriptors = providerDescriptors;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public void testConfig(final TestConfigModel testConfigModel) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfigModel.getFieldAccessor();
        final DistributionEvent event = createChannelTestEvent(testConfigModel.getConfigId().orElse(null), fieldAccessor);
        distributionChannel.sendMessage(event);
    }

    public DistributionEvent createChannelTestEvent(final String configId, final FieldAccessor fieldAccessor) {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final String channelName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse("");
        final String providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        final String formatType = fieldAccessor.getString(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).orElse("");

        return new DistributionEvent(configId, channelName, RestConstants.formatDate(new Date()), providerName, formatType, messageContent, fieldAccessor);
    }

    @Override
    public FieldModel saveConfig(final FieldModel fieldModel) {
        return getProviderActionApi(fieldModel).map(descriptorActionApi -> descriptorActionApi.saveConfig(fieldModel)).orElse(fieldModel);
    }

    @Override
    public FieldModel deleteConfig(final FieldModel fieldModel) {
        return getProviderActionApi(fieldModel).map(descriptorActionApi -> descriptorActionApi.deleteConfig(fieldModel)).orElse(fieldModel);
    }

    private Optional<DescriptorActionApi> getProviderActionApi(final FieldModel fieldModel) {
        final String providerName = fieldModel.getField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                        .flatMap(FieldValueModel::getValue)
                                        .orElse(null);
        return providerDescriptors.stream()
                   .filter(providerDescriptor -> providerDescriptor.getName().equals(providerName))
                   .findFirst()
                   .map(providerDescriptor -> providerDescriptor.getActionApi(ConfigContextEnum.DISTRIBUTION).orElse(null));
    }

    @Override
    public Map<String, String> validateCreate(final FieldModel fieldModel) {
        final String jobNameError = validateJobNameUnique(fieldModel);
        if (StringUtils.isNotBlank(jobNameError)) {
            return Map.of(ChannelDistributionUIConfig.KEY_NAME, jobNameError);
        }
        return Map.of();
    }

    private String validateJobNameUnique(final FieldModel fieldModel) {
        final String descriptorName = fieldModel.getDescriptorName();
        final Optional<FieldValueModel> jobNameFieldOptional = fieldModel.getField(ChannelDistributionUIConfig.KEY_NAME);
        if (jobNameFieldOptional.isPresent()) {
            final String jobName = jobNameFieldOptional.get().getValue().orElse(null);
            if (StringUtils.isNotBlank(jobName)) {
                try {
                    final List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorName(descriptorName);
                    final Boolean foundDuplicateName = configurations.stream()
                                                           .map(configurationModel -> configurationModel.getField(ChannelDistributionUIConfig.KEY_NAME).orElse(null))
                                                           .filter(configurationFieldModel -> (null != configurationFieldModel) && configurationFieldModel.getFieldValue().isPresent())
                                                           .anyMatch(configurationFieldModel -> jobName.equals(configurationFieldModel.getFieldValue().get()));
                    if (foundDuplicateName) {
                        return "A distribution configuration with this name already exists.";
                    }
                } catch (final AlertDatabaseConstraintException e) {
                    logger.error("Could not retrieve distributions of {}", jobName);
                }

            } else {
                return "Name cannot be blank.";
            }
        }

        return "";

    }

    private ConfigurationFieldModel getJobNameField(final ConfigurationModel configurationModel) {
        final ConfigurationFieldModel configurationFieldModel = configurationModel.getField(ChannelDistributionUIConfig.KEY_NAME).orElse(null);
        if (null != configurationFieldModel && configurationFieldModel.getFieldValue().isPresent()) {

        }

    }
}
