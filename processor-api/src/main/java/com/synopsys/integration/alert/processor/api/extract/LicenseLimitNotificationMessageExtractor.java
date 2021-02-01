/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.extract;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
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
    protected ProviderMessageHolder extract(FilterableNotificationWrapper filteredNotification, LicenseLimitNotificationContent notificationContent) {
        AlertNotificationModel alertNotificationModel = filteredNotification.getAlertNotificationModel();

        LinkableItem provider = new LinkableItem(blackDuckProviderKey.getDisplayName(), alertNotificationModel.getProviderConfigName());
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

        SimpleMessage licenseLimitMessage = SimpleMessage.original(provider, summary, notificationContent.getMessage(), details);
        return new ProviderMessageHolder(List.of(), List.of(licenseLimitMessage));
    }

}
