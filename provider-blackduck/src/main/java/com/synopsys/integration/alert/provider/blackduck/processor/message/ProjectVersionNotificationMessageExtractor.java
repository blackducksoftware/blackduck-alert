/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

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
            logger.warn("Invalid BlackDuck configuration for notification. ID: {}. Name: {}", providerConfigId, alertNotificationModel.getProviderConfigName(), e);
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
