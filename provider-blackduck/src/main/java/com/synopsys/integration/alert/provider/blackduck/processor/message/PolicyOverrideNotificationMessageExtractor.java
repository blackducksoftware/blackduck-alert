/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class PolicyOverrideNotificationMessageExtractor extends AbstractBlackDuckComponentConcernMessageExtractor<PolicyOverrideUniquePolicyNotificationContent> {
    private final BlackDuckPolicyComponentConcernCreator policyComponentConcernCreator;
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory;
    private final BomComponent404Handler bomComponent404Handler;

    @Autowired
    public PolicyOverrideNotificationMessageExtractor(
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache,
        BlackDuckPolicyComponentConcernCreator policyComponentConcernCreator,
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory,
        BomComponent404Handler bomComponent404Handler
    ) {
        super(NotificationType.POLICY_OVERRIDE, PolicyOverrideUniquePolicyNotificationContent.class, blackDuckProviderKey, servicesFactoryCache);
        this.policyComponentConcernCreator = policyComponentConcernCreator;
        this.detailsCreatorFactory = detailsCreatorFactory;
        this.bomComponent404Handler = bomComponent404Handler;
    }

    @Override
    protected List<BomComponentDetails> createBomComponentDetails(PolicyOverrideUniquePolicyNotificationContent notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = detailsCreatorFactory.createBomComponentDetailsCreator(blackDuckServicesFactory);

        ComponentConcern policyConcern = policyComponentConcernCreator.fromPolicyInfo(notificationContent.getPolicyInfo(), ItemOperation.DELETE);

        String overriderName = String.format("%s %s", notificationContent.getFirstName(), notificationContent.getLastName());
        LinkableItem overrider = new LinkableItem(BlackDuckMessageLabels.LABEL_OVERRIDER, overriderName);

        BomComponentDetails bomComponentDetails;
        try {
            ProjectVersionComponentVersionView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(notificationContent.getBomComponent()), ProjectVersionComponentVersionView.class);
            bomComponentDetails = bomComponentDetailsCreator.createBomComponentDetails(bomComponent, policyConcern, ComponentUpgradeGuidance.none(), List.of(overrider));
        } catch (IntegrationRestException e) {
            bomComponent404Handler.logIf404OrThrow(e, notificationContent.getComponentName(), notificationContent.getComponentVersionName());
            bomComponentDetails = bomComponentDetailsCreator.createMissingBomComponentDetails(
                notificationContent.getComponentName(),
                notificationContent.getBomComponent(),
                notificationContent.getComponentVersionName(),
                notificationContent.getBomComponent(),
                List.of(policyConcern),
                ComponentUpgradeGuidance.none(),
                List.of(overrider)
            );
        }
        return List.of(bomComponentDetails);
    }

}
