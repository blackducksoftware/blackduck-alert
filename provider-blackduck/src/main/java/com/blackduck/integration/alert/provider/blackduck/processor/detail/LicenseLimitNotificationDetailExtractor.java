/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractor;
import com.blackduck.integration.blackduck.api.manual.view.LicenseLimitNotificationView;

@Component
public class LicenseLimitNotificationDetailExtractor extends NotificationDetailExtractor<LicenseLimitNotificationView> {
    @Autowired
    public LicenseLimitNotificationDetailExtractor() {
        super(LicenseLimitNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, LicenseLimitNotificationView notificationView) {
        return List.of(DetailedNotificationContent.projectless(alertNotificationModel, notificationView.getContent()));
    }

}
