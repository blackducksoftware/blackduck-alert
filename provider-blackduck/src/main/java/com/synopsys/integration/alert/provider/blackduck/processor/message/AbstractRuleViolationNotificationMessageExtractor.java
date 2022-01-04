/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.model.AbstractRuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class AbstractRuleViolationNotificationMessageExtractor<T extends AbstractRuleViolationNotificationContent> extends AbstractBlackDuckComponentConcernMessageExtractor<T> {
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

    private BomComponentDetails createBomComponentDetails(BlackDuckServicesFactory blackDuckServicesFactory, T notificationContent, ComponentVersionStatus componentVersionStatus) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = detailsCreatorFactory.createBomComponentDetailsCreator(blackDuckServicesFactory);

        ComponentConcern policyConcern = policyComponentConcernCreator.fromPolicyInfo(notificationContent.getPolicyInfo(), itemOperation);
        try {
            ProjectVersionComponentVersionView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(componentVersionStatus.getBomComponent()), ProjectVersionComponentVersionView.class);
            return bomComponentDetailsCreator.createBomComponentDetails(bomComponent, policyConcern, ComponentUpgradeGuidance.none(), List.of());
        } catch (IntegrationRestException e) {
            bomComponent404Handler.logIf404OrThrow(e, componentVersionStatus.getComponentName(), componentVersionStatus.getComponentVersionName());
            return bomComponentDetailsCreator.createMissingBomComponentDetails(
                componentVersionStatus.getComponentName(),
                createComponentUrl(componentVersionStatus),
                componentVersionStatus.getComponentVersionName(),
                createComponentVersionUrl(componentVersionStatus),
                List.of(policyConcern),
                ComponentUpgradeGuidance.none(),
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
