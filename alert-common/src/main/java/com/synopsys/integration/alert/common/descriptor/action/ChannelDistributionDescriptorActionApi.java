/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.action;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionDescriptorActionApi extends DescriptorActionApi {
    private final DistributionChannel distributionChannel;
    private final List<ProviderDescriptor> providerDescriptors;

    public ChannelDistributionDescriptorActionApi(final DistributionChannel distributionChannel, final List<ProviderDescriptor> providerDescriptors) {
        this.distributionChannel = distributionChannel;
        this.providerDescriptors = providerDescriptors;
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

}
