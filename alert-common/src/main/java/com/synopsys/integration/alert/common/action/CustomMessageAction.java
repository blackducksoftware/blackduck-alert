/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action;

import java.util.Date;
import java.util.UUID;

import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.CustomMessageConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class CustomMessageAction {
    private final DistributionChannel distributionChannel;

    public CustomMessageAction(final DistributionChannel distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    public String sendMessage(CustomMessageConfigModel customMessageConfigModel) throws IntegrationException {
        final DistributionEvent event = createChannelDistributionEvent(customMessageConfigModel);
        return distributionChannel.sendMessage(event);
    }

    protected DistributionEvent createChannelDistributionEvent(CustomMessageConfigModel customMessageConfigModel) throws AlertException {
        final String configId = customMessageConfigModel.getJobId();
        final FieldAccessor fieldAccessor = customMessageConfigModel.getFieldAccessor();

        final String channelName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse("");
        final String providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        final String formatType = fieldAccessor.getString(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).orElse("");

        final String customTopic = customMessageConfigModel.getCustomTopic().orElse("Test Topic");
        final String customMessage = customMessageConfigModel.getCustomMessage().orElse("Test Message");

        final ProviderMessageContent messageContent = createCustomMessageContent(customTopic, customMessage);
        return new DistributionEvent(configId, channelName, RestConstants.formatDate(new Date()), providerName, formatType, MessageContentGroup.singleton(messageContent), fieldAccessor);
    }

    protected ProviderMessageContent createCustomMessageContent(String customTopic, String customMessage) throws AlertException {
        ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder();
        builder.applyProvider("Alert");
        builder.applyTopic("Topic", customTopic);
        builder.applyComponentItem(createCustomMessageComponent(customMessage));
        return builder.build();
    }

    private ComponentItem createCustomMessageComponent(String customMessage) throws AlertException {
        final ComponentItem.Builder builder = new ComponentItem.Builder();
        builder.applyOperation(ItemOperation.UPDATE);
        builder.applyCategory("Custom Message");
        builder.applyComponentData("Message ID", UUID.randomUUID().toString());
        builder.applyComponentData("Details", customMessage);
        builder.applyNotificationId(1L);
        return builder.build();
    }

}
