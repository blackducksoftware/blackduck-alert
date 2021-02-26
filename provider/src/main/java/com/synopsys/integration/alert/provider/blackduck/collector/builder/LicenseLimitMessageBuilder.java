/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

@Component
public class LicenseLimitMessageBuilder extends BlackDuckMessageBuilder<LicenseLimitNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(LicenseLimitMessageBuilder.class);

    @Autowired
    public LicenseLimitMessageBuilder() {
        super(NotificationType.LICENSE_LIMIT);
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, LicenseLimitNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
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
