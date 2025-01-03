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
import com.blackduck.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.ProjectVersionNotificationView;

@Component
public class ProjectVersionNotificationDetailExtractor extends NotificationDetailExtractor<ProjectVersionNotificationView> {
    @Autowired
    public ProjectVersionNotificationDetailExtractor() {
        super(ProjectVersionNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, ProjectVersionNotificationView notificationView) {
        ProjectVersionNotificationContent notificationContent = notificationView.getContent();
        return List.of(DetailedNotificationContent.project(alertNotificationModel, notificationContent, notificationContent.getProjectName(), notificationContent.getProjectVersionName()));
    }

}
