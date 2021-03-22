/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageBomComponentDetailsUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageComponentConcernUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.model.AbstractRuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class AbstractRuleViolationNotificationMessageExtractor<T extends AbstractRuleViolationNotificationContent> extends AbstractBlackDuckComponentConcernMessageExtractor<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ItemOperation itemOperation;

    public AbstractRuleViolationNotificationMessageExtractor(
        NotificationType notificationType,
        Class<T> notificationContentClass,
        ItemOperation itemOperation,
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache
    ) {
        super(notificationType, notificationContentClass, blackDuckProviderKey, servicesFactoryCache);
        this.itemOperation = itemOperation;
    }

    @Override
    protected List<BomComponentDetails> createBomComponentDetails(T notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        List<BomComponentDetails> bomComponentDetails = new LinkedList<>();
        for (ComponentVersionStatus componentVersionStatus : notificationContent.getComponentVersionStatuses()) {
            BomComponentDetails componentVersionDetails = createBomComponentDetails(blackDuckApiClient, notificationContent, componentVersionStatus);
            bomComponentDetails.add(componentVersionDetails);
        }
        return bomComponentDetails;
    }

    private BomComponentDetails createBomComponentDetails(BlackDuckApiClient blackDuckApiClient, T notificationContent, ComponentVersionStatus componentVersionStatus) throws IntegrationException {
        ComponentConcern policyConcern = BlackDuckMessageComponentConcernUtils.fromPolicyInfo(notificationContent.getPolicyInfo(), itemOperation);
        try {
            ProjectVersionComponentView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(componentVersionStatus.getBomComponent()), ProjectVersionComponentView.class);
            return BlackDuckMessageBomComponentDetailsUtils.createBomComponentDetails(bomComponent, policyConcern, List.of());
        } catch (IntegrationRestException e) {
            if (404 == e.getHttpStatusCode()) {
                logger.debug("The BOM Component '{}[{}]' no longer exists", componentVersionStatus.getComponentName(), componentVersionStatus.getComponentVersionName());
            } else {
                throw e;
            }
        }
        return BlackDuckMessageBomComponentDetailsUtils.createBomComponentDetails(
            componentVersionStatus.getComponentName(),
            componentVersionStatus.getComponent(),
            componentVersionStatus.getComponentVersionName(),
            componentVersionStatus.getComponentVersion(),
            List.of(policyConcern),
            List.of()
        );
    }

}
