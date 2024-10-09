/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageComponentVersionUpgradeGuidanceService;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.blackduck.integration.alert.provider.blackduck.processor.model.AbstractRuleViolationNotificationContent;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.exception.IntegrationRestException;

public abstract class AbstractRuleViolationNotificationMessageExtractor<T extends AbstractRuleViolationNotificationContent>
    extends AbstractBlackDuckComponentConcernMessageExtractor<T> {
    private final ItemOperation itemOperation;
    private final BlackDuckPolicyComponentConcernCreator policyComponentConcernCreator;
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory;
    private final BomComponent404Handler bomComponent404Handler;

    public AbstractRuleViolationNotificationMessageExtractor(
        NotificationType notificationType,
        Class<T> notificationContentClass,
        ItemOperation itemOperation,
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache,
        BlackDuckPolicyComponentConcernCreator policyComponentConcernCreator,
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory,
        BomComponent404Handler bomComponent404Handler
    ) {
        super(notificationType, notificationContentClass, blackDuckProviderKey, servicesFactoryCache);
        this.itemOperation = itemOperation;
        this.policyComponentConcernCreator = policyComponentConcernCreator;
        this.detailsCreatorFactory = detailsCreatorFactory;
        this.bomComponent404Handler = bomComponent404Handler;
    }

    @Override
    protected List<BomComponentDetails> createBomComponentDetails(T notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        List<BomComponentDetails> bomComponentDetails = new LinkedList<>();
        for (ComponentVersionStatus componentVersionStatus : notificationContent.getComponentVersionStatuses()) {
            BomComponentDetails componentVersionDetails = createBomComponentDetails(blackDuckServicesFactory, notificationContent, componentVersionStatus);
            bomComponentDetails.add(componentVersionDetails);
        }
        return bomComponentDetails;
    }

    private BomComponentDetails createBomComponentDetails(BlackDuckServicesFactory blackDuckServicesFactory, T notificationContent, ComponentVersionStatus componentVersionStatus)
        throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = detailsCreatorFactory.createBomComponentDetailsCreator(blackDuckServicesFactory);
        BlackDuckMessageComponentVersionUpgradeGuidanceService upgradeGuidanceService = new BlackDuckMessageComponentVersionUpgradeGuidanceService(blackDuckApiClient);

        ComponentConcern policyConcern = policyComponentConcernCreator.fromPolicyInfo(notificationContent.getPolicyInfo(), itemOperation);
        try {
            ProjectVersionComponentVersionView bomComponent = blackDuckApiClient.getResponse(
                new HttpUrl(componentVersionStatus.getBomComponent()),
                ProjectVersionComponentVersionView.class
            );
            ComponentUpgradeGuidance componentUpgradeGuidance = upgradeGuidanceService.requestUpgradeGuidanceItems(bomComponent);
            return bomComponentDetailsCreator.createBomComponentDetails(bomComponent, policyConcern, componentUpgradeGuidance, List.of());
        } catch (IntegrationRestException e) {
            bomComponent404Handler.logIf404OrThrow(e, componentVersionStatus.getComponentName(), componentVersionStatus.getComponentVersionName());
            Optional<String> componentVersion = Optional.ofNullable(componentVersionStatus.getComponentVersion());
            ComponentUpgradeGuidance componentUpgradeGuidance;
            if (componentVersion.isEmpty()) {
                componentUpgradeGuidance = ComponentUpgradeGuidance.none();
            } else {
                componentUpgradeGuidance = upgradeGuidanceService.requestUpgradeGuidanceItems(componentVersion.get());
            }
            return bomComponentDetailsCreator.createMissingBomComponentDetails(
                componentVersionStatus.getComponentName(),
                createComponentUrl(componentVersionStatus),
                componentVersionStatus.getComponentVersionName(),
                createComponentVersionUrl(componentVersionStatus),
                List.of(policyConcern),
                componentUpgradeGuidance,
                List.of()
            );
        }
    }

    private String createComponentUrl(ComponentVersionStatus status) {
        if (StringUtils.isNotBlank(status.getBomComponent()) && StringUtils.isNotBlank(status.getComponent()) && StringUtils.isNotBlank(status.getComponentName())) {
            return BlackDuckMessageLinkUtils.createComponentQueryLink(status.getBomComponent(), status.getComponentName());
        }
        return status.getComponent();
    }

    private String createComponentVersionUrl(ComponentVersionStatus status) {
        if (StringUtils.isNotBlank(status.getBomComponent()) && StringUtils.isNotBlank(status.getComponentVersion()) && StringUtils.isNotBlank(status.getComponentName())) {
            return BlackDuckMessageLinkUtils.createComponentQueryLink(status.getBomComponent(), status.getComponentName());
        }
        return status.getComponentVersion();
    }
}
