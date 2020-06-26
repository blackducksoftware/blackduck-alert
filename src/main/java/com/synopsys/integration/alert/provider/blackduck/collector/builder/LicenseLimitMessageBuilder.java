/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

@Component
public class LicenseLimitMessageBuilder extends BlackDuckMessageBuilder<LicenseLimitNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(LicenseLimitMessageBuilder.class);

    @Autowired
    public LicenseLimitMessageBuilder() {
        super(NotificationType.LICENSE_LIMIT);
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, LicenseLimitNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        LicenseLimitNotificationContent notificationContent = notificationView.getContent();
        try {
            String usageMessage = createUsageMessage(notificationContent);
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();

            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_LICENSE_LIMIT_MESSAGE, notificationContent.getMessage())
                .applySubTopic(MessageBuilderConstants.LABEL_USAGE_INFO, usageMessage)
                .applyAction(ItemOperation.INFO);
            return List.of(messageContentBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project notification messages", e);
            return List.of();
        }
    }

    private String createUsageMessage(LicenseLimitNotificationContent notificationContent) {
        return String.format("Used Code Size: %d, Hard Limit: %d, Soft Limit: %d", notificationContent.getUsedCodeSize(), notificationContent.getHardLimit(), notificationContent.getSoftLimit());
    }

}
