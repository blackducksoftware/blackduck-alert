/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.blackduck.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionWithStatusNotificationContent;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.manual.enumeration.ComponentUnknownVersionStatus;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.exception.IntegrationRestException;

@Component
public class ComponentUnknownVersionExtractor extends AbstractBlackDuckComponentConcernMessageExtractor<ComponentUnknownVersionWithStatusNotificationContent> {
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory;
    private final BomComponent404Handler bomComponent404Handler;

    @Autowired
    public ComponentUnknownVersionExtractor(BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache,
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory, BomComponent404Handler bomComponent404Handler) {
        super(NotificationType.COMPONENT_UNKNOWN_VERSION, ComponentUnknownVersionWithStatusNotificationContent.class, blackDuckProviderKey, servicesFactoryCache);
        this.detailsCreatorFactory = detailsCreatorFactory;
        this.bomComponent404Handler = bomComponent404Handler;
    }

    @Override
    protected List<BomComponentDetails> createBomComponentDetails(ComponentUnknownVersionWithStatusNotificationContent notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = detailsCreatorFactory.createBomComponentDetailsCreator(blackDuckServicesFactory);
        List<ComponentConcern> componentConcerns = createComponentConcerns(notificationContent);
        BomComponentDetails bomComponentDetails;
        try {
            ProjectVersionComponentVersionView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(notificationContent.getBomComponent()), ProjectVersionComponentVersionView.class);
            bomComponentDetails = bomComponentDetailsCreator.createBomComponentUnknownVersionDetails(bomComponent, componentConcerns, ComponentUpgradeGuidance.none(), List.of());
        } catch (IntegrationRestException e) {
            bomComponent404Handler.logIf404OrThrow(e, notificationContent.getComponentName(), null);
            bomComponentDetails = bomComponentDetailsCreator.createMissingBomComponentDetailsForUnknownVersion(
                notificationContent.getComponentName(),
                notificationContent.getBomComponent(),
                BlackDuckMessageBomComponentDetailsCreator.COMPONENT_VERSION_UNKNOWN,
                componentConcerns,
                ComponentUpgradeGuidance.none(),
                List.of()
            );
        }
        return List.of(bomComponentDetails);
    }

    private List<ComponentConcern> createComponentConcerns(ComponentUnknownVersionWithStatusNotificationContent notificationContent) {
        ComponentUnknownVersionStatus status = notificationContent.getStatus();
        String componentName = notificationContent.getComponentName();
        ItemOperation itemOperation = ComponentUnknownVersionStatus.REMOVED == status ? ItemOperation.DELETE : ItemOperation.ADD;
        ComponentConcern criticalCount = createComponentConcernWithCount(itemOperation, ComponentConcernSeverity.CRITICAL, notificationContent.getCriticalVulnerabilityCount(), componentName,
            notificationContent.getCriticalVulnerabilityVersionName(),
            notificationContent.getCriticalVulnerabilityVersion());

        ComponentConcern highCount = createComponentConcernWithCount(itemOperation, ComponentConcernSeverity.MAJOR_HIGH, notificationContent.getHighVulnerabilityCount(), componentName, notificationContent.getHighVulnerabilityVersionName(),
            notificationContent.getHighVulnerabilityVersion());

        ComponentConcern mediumCount = createComponentConcernWithCount(itemOperation, ComponentConcernSeverity.MINOR_MEDIUM, notificationContent.getMediumVulnerabilityCount(), componentName,
            notificationContent.getMediumVulnerabilityVersionName(),
            notificationContent.getMediumVulnerabilityVersion());

        ComponentConcern lowCount = createComponentConcernWithCount(itemOperation, ComponentConcernSeverity.TRIVIAL_LOW, notificationContent.getLowVulnerabilityCount(), componentName, notificationContent.getLowVulnerabilityVersionName(),
            notificationContent.getLowVulnerabilityVersion());

        return List.of(criticalCount, highCount, mediumCount, lowCount);
    }

    private ComponentConcern createComponentConcernWithCount(ItemOperation operation, ComponentConcernSeverity componentConcernSeverity, int count, String componentName, String componentVersion, String componentUrl) {
        String componentVersionName = "";
        if (count > 0) {
            componentVersionName = String.format("%s %s", componentName, componentVersion);
        }
        return ComponentConcern.unknownComponentVersion(operation, componentVersionName, componentConcernSeverity, Integer.valueOf(count), componentUrl);
    }
}
