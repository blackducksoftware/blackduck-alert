/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.channel;

import java.util.Date;
import java.util.UUID;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionTestAction extends TestAction {
    private final DistributionChannel distributionChannel;

    public ChannelDistributionTestAction(DistributionChannel distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldAccessor fieldAccessor) throws IntegrationException {
        DistributionEvent event = createChannelTestEvent(jobId, fieldAccessor);
        return distributionChannel.sendMessage(event);
    }

    public DistributionEvent createChannelTestEvent(String configId, FieldAccessor fieldAccessor) throws AlertException {
        ProviderMessageContent messageContent = createTestNotificationContent(fieldAccessor, ItemOperation.ADD, UUID.randomUUID().toString());

        String channelName = fieldAccessor.getStringOrEmpty(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        String providerConfigName = fieldAccessor.getStringOrEmpty(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        String formatType = fieldAccessor.getStringOrEmpty(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE);

        return new DistributionEvent(configId, channelName, RestConstants.formatDate(new Date()), providerConfigName, formatType, MessageContentGroup.singleton(messageContent), fieldAccessor);
    }

    public DistributionChannel getDistributionChannel() {
        return distributionChannel;
    }

}
