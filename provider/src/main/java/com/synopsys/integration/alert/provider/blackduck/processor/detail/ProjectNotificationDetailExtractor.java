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
import com.synopsys.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;

@Component
public class ProjectNotificationDetailExtractor extends NotificationDetailExtractor<ProjectNotificationContent, ProjectNotificationView> {
    @Autowired
    public ProjectNotificationDetailExtractor(Gson gson) {
        super(NotificationType.PROJECT, ProjectNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, ProjectNotificationContent projectNotificationContent) {
        return List.of(DetailedNotificationContent.project(alertNotificationModel, projectNotificationContent, projectNotificationContent.getProjectName()));
    }

}
