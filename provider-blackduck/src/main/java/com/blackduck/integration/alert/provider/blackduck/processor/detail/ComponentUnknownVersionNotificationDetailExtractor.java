/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionWithStatusNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.ComponentUnknownVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationView;

@Component
public class ComponentUnknownVersionNotificationDetailExtractor extends NotificationDetailExtractor<ComponentUnknownVersionNotificationView> {

    @Autowired
    public ComponentUnknownVersionNotificationDetailExtractor() {
        super(ComponentUnknownVersionNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, ComponentUnknownVersionNotificationView notificationView) {
        ComponentUnknownVersionNotificationContent notificationContent = notificationView.getContent();
        ComponentUnknownVersionWithStatusNotificationContent componentUnknownVersionNotificationContent = extractContents(notificationContent);
        return List.of(DetailedNotificationContent.project(alertNotificationModel, componentUnknownVersionNotificationContent, notificationContent.getProjectName(), notificationContent.getProjectVersionName()));
    }

    private ComponentUnknownVersionWithStatusNotificationContent extractContents(ComponentUnknownVersionNotificationContent notificationContent) {
        return new ComponentUnknownVersionWithStatusNotificationContent(notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            notificationContent.getComponentName(),
            notificationContent.getBomComponent(),
            notificationContent.getComponent(),
            notificationContent.getCriticalVulnerabilityCount(),
            notificationContent.getCriticalVulnerabilityVersion(),
            notificationContent.getCriticalVulnerabilityVersionName(),
            notificationContent.getHighVulnerabilityCount(),
            notificationContent.getHighVulnerabilityVersion(),
            notificationContent.getHighVulnerabilityVersionName(),
            notificationContent.getMediumVulnerabilityCount(),
            notificationContent.getMediumVulnerabilityVersion(),
            notificationContent.getMediumVulnerabilityVersionName(),
            notificationContent.getLowVulnerabilityCount(),
            notificationContent.getLowVulnerabilityVersion(),
            notificationContent.getLowVulnerabilityVersionName(),
            notificationContent.getStatus());
    }
}

