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
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class ChannelDistributionTestAction extends TestAction {
    private final DistributionChannel distributionChannel;

    public ChannelDistributionTestAction(DistributionChannel distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldUtility fieldUtility) throws IntegrationException {
        DistributionEvent event = createChannelTestEvent(jobId, fieldUtility);
        return distributionChannel.sendMessage(event);
    }

    public DistributionEvent createChannelTestEvent(String configId, FieldUtility fieldUtility) throws AlertException {
        ProviderMessageContent messageContent = createTestNotificationContent(fieldUtility, ItemOperation.ADD, UUID.randomUUID().toString());

        String channelName = fieldUtility.getStringOrEmpty(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        Long providerConfigId = fieldUtility.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).orElse(null);
        String formatType = fieldUtility.getStringOrEmpty(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE);

        return new DistributionEvent(configId, channelName, RestConstants.formatDate(new Date()), providerConfigId, formatType, MessageContentGroup.singleton(messageContent), fieldUtility);
    }

    public DistributionChannel getDistributionChannel() {
        return distributionChannel;
    }

}
