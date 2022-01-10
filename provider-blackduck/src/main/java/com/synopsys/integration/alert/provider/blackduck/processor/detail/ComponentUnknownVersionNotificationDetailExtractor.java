/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
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
import com.synopsys.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionWithStatusNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.ComponentUnknownVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationView;

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

