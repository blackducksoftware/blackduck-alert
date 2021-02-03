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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageAttributesUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageComponentConcernUtils;
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class RuleViolationNotificationMessageExtractor extends ProviderMessageExtractor<RuleViolationUniquePolicyNotificationContent> {
    private final BlackDuckProviderKey blackDuckProviderKey;

    @Autowired
    public RuleViolationNotificationMessageExtractor(BlackDuckProviderKey blackDuckProviderKey) {
        super(NotificationType.RULE_VIOLATION, RuleViolationUniquePolicyNotificationContent.class);
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    protected ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, RuleViolationUniquePolicyNotificationContent notificationContent) {
        AlertNotificationModel alertNotificationModel = notificationContentWrapper.getAlertNotificationModel();

        LinkableItem provider = new LinkableItem(blackDuckProviderKey.getDisplayName(), alertNotificationModel.getProviderConfigName());
        LinkableItem project = new LinkableItem(BlackDuckMessageConstants.LABEL_PROJECT, notificationContent.getProjectName());
        LinkableItem projectVersion = new LinkableItem(BlackDuckMessageConstants.LABEL_PROJECT_VERSION, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion());

        List<BomComponentDetails> bomComponentDetails = createBomComponentDetails(notificationContent);
        ProjectMessage ruleViolationMessage = ProjectMessage.componentConcern(provider, project, projectVersion, bomComponentDetails);

        return new ProviderMessageHolder(List.of(ruleViolationMessage), List.of());
    }

    private List<BomComponentDetails> createBomComponentDetails(RuleViolationUniquePolicyNotificationContent notificationContent) {
        List<BomComponentDetails> bomComponentDetails = new LinkedList<>();
        for (ComponentVersionStatus componentVersionStatus : notificationContent.getComponentVersionStatuses()) {
            ComponentConcern policyConcern = BlackDuckMessageComponentConcernUtils.fromPolicyInfo(notificationContent.getPolicyInfo());
            BomComponentDetails componentVersionDetails = createBomComponentDetails(notificationContent, componentVersionStatus, policyConcern);
            bomComponentDetails.add(componentVersionDetails);
        }
        return bomComponentDetails;
    }

    private BomComponentDetails createBomComponentDetails(RuleViolationUniquePolicyNotificationContent notificationContent, ComponentVersionStatus componentVersionStatus, ComponentConcern componentConcern) {
        // FIXME use "query links"
        LinkableItem component = new LinkableItem(BlackDuckMessageConstants.LABEL_COMPONENT, componentVersionStatus.getComponentName(), componentVersionStatus.getComponent());
        LinkableItem componentVersion = null;
        String componentVersionUrl = componentVersionStatus.getComponentVersion();
        if (StringUtils.isNotBlank(componentVersionUrl)) {
            componentVersion = new LinkableItem(BlackDuckMessageConstants.LABEL_COMPONENT_VERSION, componentVersionStatus.getComponentVersionName(), componentVersionUrl);
        }

        ProjectVersionComponentView bomComponent = retrieveBomComponent(componentVersionStatus.getBomComponent());

        LinkableItem licenseInfo = BlackDuckMessageAttributesUtils.extractLicense(bomComponent);
        String usageInfo = BlackDuckMessageAttributesUtils.extractUsage(bomComponent);
        String issuesUrl = BlackDuckMessageAttributesUtils.extractIssuesUrl(bomComponent).orElse(null);

        return new BomComponentDetails(component, componentVersion, List.of(componentConcern), licenseInfo, usageInfo, List.of(), issuesUrl);
    }

    private ProjectVersionComponentView retrieveBomComponent(String bomComponentUrl) {
        // FIXME retrieve
        return null;
    }

}
