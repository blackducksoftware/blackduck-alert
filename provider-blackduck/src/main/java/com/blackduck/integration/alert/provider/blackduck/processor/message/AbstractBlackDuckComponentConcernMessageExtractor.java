/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractor;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.provider.blackduck.processor.model.AbstractProjectVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;

public abstract class AbstractBlackDuckComponentConcernMessageExtractor<T extends AbstractProjectVersionNotificationContent> extends ProviderMessageExtractor<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache;

    public AbstractBlackDuckComponentConcernMessageExtractor(
        NotificationType notificationType,
        Class<T> notificationContentClass,
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache
    ) {
        super(notificationType, notificationContentClass);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    protected final ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, T notificationContent) {
        AlertNotificationModel notificationModel = notificationContentWrapper.getAlertNotificationModel();

        Long providerConfigId = notificationModel.getProviderConfigId();
        String providerUrl;

        List<BomComponentDetails> bomComponentDetails;
        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(providerConfigId);
            providerUrl = blackDuckServicesFactory.getBlackDuckHttpClient().getBlackDuckUrl().string();
            bomComponentDetails = createBomComponentDetails(notificationContent, blackDuckServicesFactory);
        } catch (AlertConfigurationException e) {
            logger.warn("Invalid Black Duck configuration for notification. ID: {}. Name: {}", providerConfigId, notificationModel.getProviderConfigName(), e);
            return ProviderMessageHolder.empty();
        } catch (IntegrationException e) {
            logger.warn("Failed to retrieve BOM Component(s) from Black Duck", e);
            return ProviderMessageHolder.empty();
        }

        LinkableItem providerItem = new LinkableItem(blackDuckProviderKey.getDisplayName(), notificationModel.getProviderConfigName(), providerUrl);
        ProviderDetails providerDetails = new ProviderDetails(notificationModel.getProviderConfigId(), providerItem);

        Optional<String> projectUrl = extractProjectUrl(notificationContent.getProjectVersionUrl());
        LinkableItem project = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT, notificationContent.getProjectName(), projectUrl.orElse(null));
        // IALERT-3119: For Project Version, redirect to components tab
        LinkableItem projectVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT_VERSION, notificationContent.getProjectVersionName(), notificationContent.getProjectVersionComponentsTabUrl());

        // FIXME this is where I should put the additional info
        ProjectMessage projectMessage = createProjectMessage(providerDetails, project, projectVersion, bomComponentDetails);
        return new ProviderMessageHolder(List.of(projectMessage), List.of());
    }

    protected ProjectMessage createProjectMessage(ProviderDetails provider, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponentDetails) {
        return ProjectMessage.componentConcern(provider, project, projectVersion, bomComponentDetails);
    }

    protected abstract List<BomComponentDetails> createBomComponentDetails(T notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException;

    private Optional<String> extractProjectUrl(String projectVersionUrl) {
        return Optional.ofNullable(projectVersionUrl)
            .filter(StringUtils::isNotBlank)
            .map(url -> StringUtils.substringBefore(url, "/versions"));
    }

}
