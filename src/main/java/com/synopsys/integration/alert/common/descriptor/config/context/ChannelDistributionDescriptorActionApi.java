/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionDescriptorActionApi extends DescriptorActionApi {
    private final DistributionChannel distributionChannel;
    private final BaseConfigurationAccessor configurationAccessor;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ContentConverter contentConverter;
    private final List<ProviderDescriptor> providerDescriptors;

    public ChannelDistributionDescriptorActionApi(final DistributionChannel distributionChannel, final BaseConfigurationAccessor configurationAccessor, final ContentConverter contentConverter,
        final List<ProviderDescriptor> providerDescriptors) {
        this.distributionChannel = distributionChannel;
        this.configurationAccessor = configurationAccessor;
        this.contentConverter = contentConverter;
        this.providerDescriptors = providerDescriptors;
    }

    @Override
    public void testConfig(final TestConfigModel testConfigModel) throws IntegrationException {
        final FieldModel fieldModel = testConfigModel.getFieldModel();
        final DistributionEvent event = createChannelTestEvent(fieldModel);
        distributionChannel.sendMessage(event);
    }

    public abstract void validateChannelConfig(FieldAccessor fieldAccessor, Map<String, String> fieldErrors);

    public DistributionEvent createChannelTestEvent(final FieldModel fieldModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final String channelName = fieldModel.getField(CommonDistributionUIConfig.KEY_CHANNEL_NAME).getValue().orElse("");
        final String providerName = fieldModel.getField(CommonDistributionUIConfig.KEY_PROVIDER_NAME).getValue().orElse("");
        final String formatType = fieldModel.getField(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).getValue().orElse("");

        final FieldAccessor fieldAccessor = fieldModel.convertToFieldAccessor();

        return new DistributionEvent(fieldModel.getId(), channelName, RestConstants.formatDate(new Date()), providerName, formatType, messageContent, fieldAccessor);
    }

    // TODO this has references to Blackduck for verification and will need to be removed.
    @Override
    public void validateConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String descriptorName = distributionChannel.getDistributionType();

        final String jobName = fieldAccessor.getString(CommonDistributionUIConfig.KEY_NAME).orElse(null);
        if (StringUtils.isNotBlank(jobName)) {
            try {
                final List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorName(descriptorName);
                final Boolean foundDuplicateName = configurations.stream()
                                                       .map(configurationModel -> configurationModel.getField(CommonDistributionUIConfig.KEY_NAME).orElse(null))
                                                       .filter(configurationFieldModel -> (null != configurationFieldModel) && configurationFieldModel.getFieldValue().isPresent())
                                                       .anyMatch(configurationFieldModel -> jobName.equals(configurationFieldModel.getFieldValue().get()));
                if (foundDuplicateName) {
                    fieldErrors.put(CommonDistributionUIConfig.KEY_NAME, "A distribution configuration with this name already exists.");
                }
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Could not retrieve distributions of {}", jobName);
            }

        } else {
            fieldErrors.put(CommonDistributionUIConfig.KEY_NAME, "Name cannot be blank.");
        }
        if (StringUtils.isBlank(fieldAccessor.getString(CommonDistributionUIConfig.KEY_CHANNEL_NAME).orElse(null))) {
            fieldErrors.put(CommonDistributionUIConfig.KEY_CHANNEL_NAME, "You must choose a distribution type.");
        }
        if (StringUtils.isBlank(CommonDistributionUIConfig.KEY_PROVIDER_NAME)) {
            fieldErrors.put(CommonDistributionUIConfig.KEY_PROVIDER_NAME, "You must choose a provider.");
        }

        validateChannelConfig(fieldAccessor, fieldErrors);
    }

    @Override
    public void saveConfig(final FieldModel fieldModel) {
        final DescriptorActionApi providerActionApi = getProviderActionApi(fieldModel);
        if (null != providerActionApi) {
            providerActionApi.saveConfig(fieldModel);
        }
        super.saveConfig(fieldModel);
    }

    @Override
    public void deleteConfig(final FieldModel fieldModel) {
        final DescriptorActionApi descriptorActionApi = getProviderActionApi(fieldModel);
        if (null != descriptorActionApi) {
            descriptorActionApi.deleteConfig(fieldModel);
        }
        super.deleteConfig(fieldModel);
    }

    private DescriptorActionApi getProviderActionApi(final FieldModel fieldModel) {
        final FieldAccessor fieldAccessor = fieldModel.convertToFieldAccessor();
        final String providerName = fieldAccessor.getString(CommonDistributionUIConfig.KEY_PROVIDER_NAME).orElse(null);
        final Optional<ProviderDescriptor> foundProviderDescriptor = providerDescriptors.stream()
                                                                         .filter(providerDescriptor -> providerDescriptor.getName().equals(providerName))
                                                                         .findFirst();
        if (foundProviderDescriptor.isPresent()) {
            return foundProviderDescriptor.get().getRestApi(ConfigContextEnum.DISTRIBUTION);
        }

        return null;
    }
}
