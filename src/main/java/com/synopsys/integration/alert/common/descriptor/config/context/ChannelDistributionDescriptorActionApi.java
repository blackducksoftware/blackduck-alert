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
import java.util.Optional;

import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionDescriptorActionApi extends DescriptorActionApi {
    private final DistributionChannel distributionChannel;
    private final List<ProviderDescriptor> providerDescriptors;
    private final ConfigurationFieldModelConverter modelConverter;

    public ChannelDistributionDescriptorActionApi(final DistributionChannel distributionChannel, final List<ProviderDescriptor> providerDescriptors, final ConfigurationFieldModelConverter modelConverter) {
        this.distributionChannel = distributionChannel;
        this.providerDescriptors = providerDescriptors;
        this.modelConverter = modelConverter;
    }

    @Override
    public void testConfig(final TestConfigModel testConfigModel) throws IntegrationException {
        final FieldModel fieldModel = testConfigModel.getFieldModel();
        final DistributionEvent event = createChannelTestEvent(fieldModel);
        distributionChannel.sendMessage(event);
    }

    public DistributionEvent createChannelTestEvent(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final String channelName = fieldModel.getField(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).flatMap(field -> field.getValue()).orElse("");
        final String providerName = fieldModel.getField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).flatMap(field -> field.getValue()).orElse("");
        final String formatType = fieldModel.getField(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).flatMap(field -> field.getValue()).orElse("");

        final FieldAccessor fieldAccessor = modelConverter.convertToFieldAccessor(fieldModel);

        return new DistributionEvent(fieldModel.getId(), channelName, RestConstants.formatDate(new Date()), providerName, formatType, messageContent, fieldAccessor);
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
                                        .flatMap(fieldValueModel -> fieldValueModel.getValue())
                                        .orElse(null);
        return providerDescriptors.stream()
                   .filter(providerDescriptor -> providerDescriptor.getName().equals(providerName))
                   .findFirst()
                   .map(providerDescriptor -> providerDescriptor.getActionApi(ConfigContextEnum.DISTRIBUTION).orElse(null));
    }
}
