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
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;

@Component
public class ProjectVersionNotificationDetailExtractor extends NotificationDetailExtractor<ProjectVersionNotificationContent, ProjectVersionNotificationView> {
    @Autowired
    public ProjectVersionNotificationDetailExtractor(Gson gson) {
        super(NotificationType.PROJECT_VERSION, ProjectVersionNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, ProjectVersionNotificationContent notificationContent) {
        return List.of(DetailedNotificationContent.project(alertNotificationModel, notificationContent, notificationContent.getProjectName()));
    }

}
