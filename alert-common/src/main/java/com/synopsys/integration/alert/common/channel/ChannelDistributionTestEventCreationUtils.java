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

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.rest.RestConstants;

public final class ChannelDistributionTestEventCreationUtils {
    public static DistributionEvent createChannelTestEvent(String customTopic, String customMessage, DistributionJobModel distributionJobModel, ConfigurationModel channelGlobalConfig) throws AlertException {
        ProviderMessageContent messageContent = createTestNotificationContent(customTopic, customMessage, ItemOperation.ADD, UUID.randomUUID().toString());

        String channelName = distributionJobModel.getChannelDescriptorName();
        Long providerConfigId = distributionJobModel.getBlackDuckGlobalConfigId();
        String processingType = distributionJobModel.getProcessingType().name();

        return new DistributionEvent(channelName, RestConstants.formatDate(new Date()), providerConfigId, processingType, MessageContentGroup.singleton(messageContent), distributionJobModel, channelGlobalConfig);
    }

    public static ProviderMessageContent createTestNotificationContent(String customTopic, String customMessage, ItemOperation operation, String messageId) throws AlertException {
        return new ProviderMessageContent.Builder()
                   .applyProvider("Alert", ProviderProperties.UNKNOWN_CONFIG_ID, "Test")
                   .applyTopic("Test Topic", customTopic)
                   .applySubTopic("Test SubTopic", "Test message sent by Alert")
                   .applyComponentItem(createTestComponentItem(operation, messageId, customMessage))
                   .build();
    }

    private static ComponentItem createTestComponentItem(ItemOperation operation, String messageId, String customMessage) throws AlertException {
        return new ComponentItem.Builder()
                   .applyOperation(operation)
                   .applyCategory("Test Category")
                   .applyComponentData("Message ID", messageId)
                   .applyCategoryItem("Details", customMessage)
                   .applyNotificationId(1L)
                   .build();
    }

}
