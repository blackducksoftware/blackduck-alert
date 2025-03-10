/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractor;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class ProjectVersionNotificationMessageExtractor extends ProviderMessageExtractor<ProjectVersionNotificationContent> {
    private final Logger logger = LoggerFactory.getLogger(ProjectVersionNotificationMessageExtractor.class);
    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache;

    @Autowired
    public ProjectVersionNotificationMessageExtractor(BlackDuckProviderKey blackDuckProviderKey, NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache) {
        super(NotificationType.PROJECT_VERSION, ProjectVersionNotificationContent.class);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    protected ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, ProjectVersionNotificationContent notificationContent) {
        AlertNotificationModel alertNotificationModel = notificationContentWrapper.getAlertNotificationModel();
        Long providerConfigId = alertNotificationModel.getProviderConfigId();
        String providerUrl;

        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(providerConfigId);
            providerUrl = blackDuckServicesFactory.getBlackDuckHttpClient().getBlackDuckUrl().string();
        } catch (AlertConfigurationException e) {
            logger.warn("Invalid Black Duck configuration for notification. ID: {}. Name: {}", providerConfigId, alertNotificationModel.getProviderConfigName(), e);
            return ProviderMessageHolder.empty();
        }
        LinkableItem providerItem = new LinkableItem(blackDuckProviderKey.getDisplayName(), alertNotificationModel.getProviderConfigName(), providerUrl);
        ProviderDetails providerDetails = new ProviderDetails(alertNotificationModel.getProviderConfigId(), providerItem);

        LinkableItem project = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT, notificationContent.getProjectName(), notificationContent.getProject());
        LinkableItem projectVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT_VERSION, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion());
        ProjectOperation operation = ProjectOperation.fromOperationType(notificationContent.getOperationType());

        ProjectMessage projectVersionMessage = ProjectMessage.projectVersionStatusInfo(providerDetails, project, projectVersion, operation);
        return new ProviderMessageHolder(List.of(projectVersionMessage), List.of());
    }

}
