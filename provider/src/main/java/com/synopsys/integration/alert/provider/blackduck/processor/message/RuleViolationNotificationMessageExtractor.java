/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.processor.ProcessorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageAttributesUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageComponentConcernUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class RuleViolationNotificationMessageExtractor extends ProviderMessageExtractor<RuleViolationUniquePolicyNotificationContent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final ProcessorBlackDuckServicesFactoryCache servicesFactoryCache;

    @Autowired
    public RuleViolationNotificationMessageExtractor(BlackDuckProviderKey blackDuckProviderKey, ProcessorBlackDuckServicesFactoryCache servicesFactoryCache) {
        super(NotificationType.RULE_VIOLATION, RuleViolationUniquePolicyNotificationContent.class);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    protected ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, RuleViolationUniquePolicyNotificationContent notificationContent) {
        AlertNotificationModel notificationModel = notificationContentWrapper.getAlertNotificationModel();

        List<BomComponentDetails> bomComponentDetails;
        Long providerConfigId = notificationModel.getProviderConfigId();
        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(providerConfigId);
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            bomComponentDetails = createBomComponentDetails(notificationContent, blackDuckApiClient);
        } catch (AlertConfigurationException e) {
            logger.warn("Invalid BlackDuck configuration for notification. ID: {}. Name: {}", providerConfigId, notificationModel.getProviderConfigName(), e);
            return ProviderMessageHolder.empty();
        } catch (IntegrationException e) {
            logger.warn("Failed to retrieve BOM Component(s) from BlackDuck", e);
            return ProviderMessageHolder.empty();
        }

        LinkableItem provider = new LinkableItem(blackDuckProviderKey.getDisplayName(), notificationModel.getProviderConfigName());
        LinkableItem project = new LinkableItem(BlackDuckMessageConstants.LABEL_PROJECT, notificationContent.getProjectName());
        LinkableItem projectVersion = new LinkableItem(BlackDuckMessageConstants.LABEL_PROJECT_VERSION, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion());

        ProjectMessage ruleViolationMessage = ProjectMessage.componentConcern(provider, project, projectVersion, bomComponentDetails);
        return new ProviderMessageHolder(List.of(ruleViolationMessage), List.of());
    }

    private List<BomComponentDetails> createBomComponentDetails(RuleViolationUniquePolicyNotificationContent notificationContent, BlackDuckApiClient blackDuckApiClient) throws IntegrationException {
        List<BomComponentDetails> bomComponentDetails = new LinkedList<>();
        for (ComponentVersionStatus componentVersionStatus : notificationContent.getComponentVersionStatuses()) {
            ProjectVersionComponentView bomComponent = blackDuckApiClient.getResponse(new HttpUrl(componentVersionStatus.getBomComponent()), ProjectVersionComponentView.class);
            ComponentConcern policyConcern = BlackDuckMessageComponentConcernUtils.fromPolicyInfo(notificationContent.getPolicyInfo());

            BomComponentDetails componentVersionDetails = createBomComponentDetails(bomComponent, policyConcern);
            bomComponentDetails.add(componentVersionDetails);
        }
        return bomComponentDetails;
    }

    private BomComponentDetails createBomComponentDetails(ProjectVersionComponentView bomComponent, ComponentConcern componentConcern) {
        LinkableItem component;
        LinkableItem componentVersion = null;

        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);

        String componentVersionUrl = bomComponent.getComponentVersion();
        if (StringUtils.isNotBlank(componentVersionUrl)) {
            component = new LinkableItem(BlackDuckMessageConstants.LABEL_COMPONENT, bomComponent.getComponentName());
            componentVersion = new LinkableItem(BlackDuckMessageConstants.LABEL_COMPONENT_VERSION, bomComponent.getComponentVersionName(), componentQueryLink);
        } else {
            component = new LinkableItem(BlackDuckMessageConstants.LABEL_COMPONENT, bomComponent.getComponentName(), componentQueryLink);
        }

        LinkableItem licenseInfo = BlackDuckMessageAttributesUtils.extractLicense(bomComponent);
        String usageInfo = BlackDuckMessageAttributesUtils.extractUsage(bomComponent);
        String issuesUrl = BlackDuckMessageAttributesUtils.extractIssuesUrl(bomComponent).orElse(null);

        return new BomComponentDetails(component, componentVersion, List.of(componentConcern), licenseInfo, usageInfo, List.of(), issuesUrl);
    }

}
