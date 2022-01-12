/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.model.BomEditWithProjectNameNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class BomEditNotificationDetailExtractor extends NotificationDetailExtractor<BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache;

    @Autowired
    public BomEditNotificationDetailExtractor(NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache) {
        super(BomEditNotificationView.class);
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, BomEditNotificationView notificationView) {
        BomEditNotificationContent notificationContent = notificationView.getContent();
        Optional<ProjectVersionWrapper> optionalProjectAndVersion = retrieveProjectAndVersion(alertNotificationModel.getProviderConfigId(), notificationContent.getProjectVersion());
        if (optionalProjectAndVersion.isPresent()) {
            ProjectVersionWrapper projectAndVersion = optionalProjectAndVersion.get();
            ProjectView project = projectAndVersion.getProjectView();
            ProjectVersionView projectVersion = projectAndVersion.getProjectVersionView();

            String projectName = project.getName();
            BomEditWithProjectNameNotificationContent updatedNotificationContent = new BomEditWithProjectNameNotificationContent(notificationContent, projectName, projectVersion.getVersionName());
            DetailedNotificationContent detailedContent = DetailedNotificationContent.project(alertNotificationModel, updatedNotificationContent, projectName, projectVersion.getVersionName());
            return List.of(detailedContent);
        }
        return List.of();
    }

    private Optional<ProjectVersionWrapper> retrieveProjectAndVersion(Long blackDuckConfigId, String projectVersionUrl) {
        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            ProjectVersionView projectVersion = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
            ProjectView projectView = blackDuckApiClient.getResponse(projectVersion.metaProjectLink());
            return Optional.of(new ProjectVersionWrapper(projectView, projectVersion));
        } catch (IntegrationException e) {
            logger.error("Failed to connect to BlackDuck. Config ID: {}", blackDuckConfigId, e);
        }
        return Optional.empty();
    }

}
