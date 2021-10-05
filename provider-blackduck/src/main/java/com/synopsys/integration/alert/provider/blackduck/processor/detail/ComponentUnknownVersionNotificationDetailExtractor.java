/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.blackduck.api.manual.component.ComponentUnknownVersionContent;
import com.synopsys.integration.blackduck.api.manual.view.ComponentUnknownVersionView;

@Component
public class ComponentUnknownVersionNotificationDetailExtractor extends NotificationDetailExtractor<ComponentUnknownVersionView> {

    @Autowired
    public ComponentUnknownVersionNotificationDetailExtractor() {
        super(ComponentUnknownVersionView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, ComponentUnknownVersionView notificationView) {
        ComponentUnknownVersionContent notificationContent = notificationView.getContent();
        return List.of(DetailedNotificationContent.project(alertNotificationModel, notificationContent, notificationContent.getProjectName()));
    }
}

