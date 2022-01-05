/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class LicenseLimitNotificationMessageExtractor extends ProviderMessageExtractor<LicenseLimitNotificationContent> {
    private final BlackDuckProviderKey blackDuckProviderKey;

    @Autowired
    public LicenseLimitNotificationMessageExtractor(BlackDuckProviderKey blackDuckProviderKey) {
        super(NotificationType.LICENSE_LIMIT, LicenseLimitNotificationContent.class);
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    protected ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, LicenseLimitNotificationContent notificationContent) {
        AlertNotificationModel alertNotificationModel = notificationContentWrapper.getAlertNotificationModel();

        LinkableItem providerItem = new LinkableItem(blackDuckProviderKey.getDisplayName(), alertNotificationModel.getProviderConfigName());
        ProviderDetails providerDetails = new ProviderDetails(alertNotificationModel.getProviderConfigId(), providerItem);

        String summary = "License Limit Event";

        List<LinkableItem> details = new LinkedList<>();

        String marketingPageUrl = notificationContent.getMarketingPageUrl();
        if (StringUtils.isNotBlank(marketingPageUrl)) {
            LinkableItem marketingDetail = new LinkableItem("Marketing Page", "Visit", marketingPageUrl);
            details.add(marketingDetail);
        }

        Long usedCodeSize = notificationContent.getUsedCodeSize();
        if (null != usedCodeSize) {
            LinkableItem usedCodeSizeDetail = new LinkableItem("Used Code Size", usedCodeSize.toString());
            details.add(usedCodeSizeDetail);
        }

        Long hardLimit = notificationContent.getHardLimit();
        if (null != hardLimit) {
            LinkableItem hardLimitDetail = new LinkableItem("Hard Limit", hardLimit.toString());
            details.add(hardLimitDetail);
        }

        Long softLimit = notificationContent.getSoftLimit();
        if (null != softLimit) {
            LinkableItem softLimitDetail = new LinkableItem("Soft Limit", softLimit.toString());
            details.add(softLimitDetail);
        }

        SimpleMessage licenseLimitMessage = SimpleMessage.original(providerDetails, summary, notificationContent.getMessage(), details);
        return new ProviderMessageHolder(List.of(), List.of(licenseLimitMessage));
    }

}
