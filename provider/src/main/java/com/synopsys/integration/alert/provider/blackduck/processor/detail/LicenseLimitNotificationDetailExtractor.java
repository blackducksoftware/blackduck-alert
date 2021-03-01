/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;

@Component
public class LicenseLimitNotificationDetailExtractor extends NotificationDetailExtractor<LicenseLimitNotificationContent, LicenseLimitNotificationView> {
    @Autowired
    public LicenseLimitNotificationDetailExtractor(Gson gson) {
        super(NotificationType.LICENSE_LIMIT, LicenseLimitNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, LicenseLimitNotificationContent notificationContent) {
        return List.of(DetailedNotificationContent.projectless(alertNotificationModel, notificationContent));
    }

}
