/*
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.model.BomEditWithProjectNameNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class BomEditNotificationDetailExtractor extends NotificationDetailExtractor<BomEditNotificationContent, BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache;

    @Autowired
    public BomEditNotificationDetailExtractor(Gson gson, NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache) {
        super(NotificationType.BOM_EDIT, BomEditNotificationView.class, gson);
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, BomEditNotificationContent notificationContent) {
        Optional<ProjectVersionWrapper> optionalProjectAndVersion = retrieveProjectAndVersion(alertNotificationModel.getProviderConfigId(), notificationContent.getProjectVersion());
        if (optionalProjectAndVersion.isPresent()) {
            ProjectVersionWrapper projectAndVersion = optionalProjectAndVersion.get();
            ProjectView project = projectAndVersion.getProjectView();
            ProjectVersionView projectVersion = projectAndVersion.getProjectVersionView();

            String projectName = project.getName();
            BomEditWithProjectNameNotificationContent updatedNotificationContent = new BomEditWithProjectNameNotificationContent(notificationContent, project.getName(), projectVersion.getVersionName());
            DetailedNotificationContent detailedContent = DetailedNotificationContent.project(alertNotificationModel, updatedNotificationContent, projectName);
            return List.of(detailedContent);
        }
        return List.of();
    }

    private Optional<ProjectVersionWrapper> retrieveProjectAndVersion(Long blackDuckConfigId, String projectVersionUrl) {
        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            ProjectVersionView projectVersion = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
            return blackDuckApiClient.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE)
                       .map(project -> new ProjectVersionWrapper(project, projectVersion));
        } catch (IntegrationException e) {
            logger.error("Failed to connect to BlackDuck. Config ID: {}", blackDuckConfigId, e);
        }
        return Optional.empty();
    }

}
