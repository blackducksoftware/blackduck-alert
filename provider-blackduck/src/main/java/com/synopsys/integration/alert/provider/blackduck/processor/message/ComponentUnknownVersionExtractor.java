/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.ComponentUnknownVersionStatus;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class ComponentUnknownVersionExtractor extends AbstractBlackDuckComponentConcernMessageExtractor<ComponentUnknownVersionNotificationContent> {
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory;
    private final BomComponent404Handler bomComponent404Handler;

    @Autowired
    public ComponentUnknownVersionExtractor(BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache,
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory, BomComponent404Handler bomComponent404Handler) {
        super(NotificationType.COMPONENT_UNKNOWN_VERSION, ComponentUnknownVersionNotificationContent.class, blackDuckProviderKey, servicesFactoryCache);
        this.detailsCreatorFactory = detailsCreatorFactory;
        this.bomComponent404Handler = bomComponent404Handler;
    }

    @Override
    protected List<BomComponentDetails> createBomComponentDetails(ComponentUnknownVersionNotificationContent notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = detailsCreatorFactory.createBomComponentDetailsCreator(blackDuckApiClient);
        List<ComponentConcern> componentConcerns = createComponentConcerns(notificationContent);
        List<LinkableItem> additionAttributes = createAdditionalAttributes(notificationContent);
        BomComponentDetails bomComponentDetails;
        try {
            ProjectVersionComponentVersionView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(notificationContent.getBomComponent()), ProjectVersionComponentVersionView.class);
            bomComponentDetails = bomComponentDetailsCreator.createBomComponentUnknownVersionDetails(bomComponent, componentConcerns, additionAttributes);
        } catch (IntegrationRestException e) {
            bomComponent404Handler.logIf404OrThrow(e, notificationContent.getComponentName(), null);
            bomComponentDetails = bomComponentDetailsCreator.createMissingBomComponentDetails(
                notificationContent.getComponentName(),
                notificationContent.getBomComponent(),
                null,
                null,
                componentConcerns,
                additionAttributes
            );
        }
        return List.of(bomComponentDetails);
    }

    private List<ComponentConcern> createComponentConcerns(ComponentUnknownVersionNotificationContent notificationContent) {
        ComponentUnknownVersionStatus status = notificationContent.getStatus();
        ItemOperation itemOperation = ComponentUnknownVersionStatus.REMOVED == status ? ItemOperation.DELETE : ItemOperation.ADD;
        return List.of(ComponentConcern.unknownComponentVersion(itemOperation, notificationContent.getComponentName(), notificationContent.getComponent()));
    }

    private List<LinkableItem> createAdditionalAttributes(ComponentUnknownVersionNotificationContent notificationContent) {
        List<LinkableItem> additionalAttributes = new ArrayList<>(5);
        String componentName = notificationContent.getComponentName();
        LinkableItem vulnerabilityCountsSection = new LinkableItem("Vulnerability Counts", "");
        LinkableItem criticalCounts = createCountLinkableItem(ComponentConcernSeverity.CRITICAL, notificationContent.getCriticalVulnerabilityCount(), componentName, notificationContent.getCriticalVulnerabilityName(),
            notificationContent.getCriticalVulnerabilityVersion());
        LinkableItem highCounts = createCountLinkableItem(ComponentConcernSeverity.MAJOR_HIGH, notificationContent.getHighVulnerabilityCount(), componentName, notificationContent.getHighVulnerabilityVersionName(),
            notificationContent.getHighVulnerabilityVersion());
        LinkableItem mediumCounts = createCountLinkableItem(ComponentConcernSeverity.MINOR_MEDIUM, notificationContent.getMediumVulnerabilityCount(), componentName, notificationContent.getMediumVulnerabilityVersionName(),
            notificationContent.getMediumVulnerabilityVersion());
        LinkableItem lowCounts = createCountLinkableItem(ComponentConcernSeverity.TRIVIAL_LOW, notificationContent.getLowVulnerabilityCount(), componentName, notificationContent.getLowVulnerabilityVersionName(),
            notificationContent.getLowVulnerabilityVersion());

        additionalAttributes.add(vulnerabilityCountsSection);
        additionalAttributes.add(criticalCounts);
        additionalAttributes.add(highCounts);
        additionalAttributes.add(mediumCounts);
        additionalAttributes.add(lowCounts);

        return additionalAttributes;
    }

    private LinkableItem createCountLinkableItem(ComponentConcernSeverity componentConcernSeverity, int count, String componentName, String componentVersion, String componentUrl) {
        String value = "";
        if (count > 0) {
            value = String.format("%s %s", componentName, componentVersion);
        }
        return new LinkableItem(String.format("    %s (%d)", componentConcernSeverity.getVulnerabilityLabel(), count), value, componentUrl);
    }
}
