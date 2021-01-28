/**
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.AlertBlackDuckService;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionVulnerableBomComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BomEditMessageBuilder extends BlackDuckMessageBuilder<BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(BomEditMessageBuilder.class);
    private final PolicyCommonBuilder policyCommonBuilder;
    private final BlackDuckIssueTrackerCallbackUtility blackDuckIssueTrackerCallbackUtility;

    @Autowired
    public BomEditMessageBuilder(PolicyCommonBuilder policyCommonBuilder, BlackDuckIssueTrackerCallbackUtility blackDuckIssueTrackerCallbackUtility) {
        super(NotificationType.BOM_EDIT);
        this.policyCommonBuilder = policyCommonBuilder;
        this.blackDuckIssueTrackerCallbackUtility = blackDuckIssueTrackerCallbackUtility;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, BomEditNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        AlertBlackDuckService alertBlackDuckService = new AlertBlackDuckService(blackDuckApiClient);
        BomEditNotificationContent bomEditContent = notificationView.getContent();

        Long notificationId = commonMessageData.getNotificationId();

        Optional<ProjectVersionComponentView> projectVersionComponentViewOptional = alertBlackDuckService.getBomComponentView(bomEditContent.getBomComponent());
        Optional<ProjectVersionWrapper> projectVersionWrapperOptional = alertBlackDuckService.getProjectVersionWrapper(bomEditContent.getProjectVersion());
        if (projectVersionComponentViewOptional.isPresent() && projectVersionWrapperOptional.isPresent()) {
            try {
                ProjectVersionComponentView bomComponent = projectVersionComponentViewOptional.get();
                ProjectVersionWrapper projectVersionWrapper = projectVersionWrapperOptional.get();

                ProjectView projectView = projectVersionWrapper.getProjectView();
                ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
                messageContentBuilder
                    .applyCommonData(commonMessageData)
                    .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, projectView.getName(), projectView.getHref().toString())
                    .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, projectVersionWrapper.getProjectVersionView().getVersionName(),
                        projectVersionWrapper.getProjectVersionView().getHref().toString());

                List<LinkableItem> commonAttributes = Stream.concat(ComponentBuilderUtil.getLicenseLinkableItems(bomComponent).stream(), ComponentBuilderUtil.getUsageLinkableItems(bomComponent).stream())
                                                          .collect(Collectors.toList());

                List<ComponentItem> componentItems = new LinkedList<>(addVulnerabilityData(alertBlackDuckService, componentService, notificationId, bomComponent, projectVersionWrapper, commonAttributes));
                componentItems.addAll(createPolicyItems(alertBlackDuckService, blackDuckApiClient, notificationId, projectVersionWrapper, bomComponent, commonAttributes));

                messageContentBuilder.applyAllComponentItems(componentItems);
                return List.of(messageContentBuilder.build());
            } catch (IntegrationException ex) {
                logger.error("Error creating policy violation message.", ex);
            }
        }
        return List.of();
    }

    private Collection<ComponentItem> addVulnerabilityData(AlertBlackDuckService alertBlackDuckService, ComponentService componentService, Long notificationId, ProjectVersionComponentView versionBomComponent,
        ProjectVersionWrapper projectVersionWrapper, List<LinkableItem> commonAttributes) {
        Collection<ComponentItem> items = new LinkedList<>();
        RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
        String componentName = versionBomComponent.getComponentName();
        String componentVersionName = versionBomComponent.getComponentVersionName();
        String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().toString();
        try {
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.VULNERABLE_COMPONENTS_LINK);
            if (VulnerabilityUtil.doesSecurityRiskProfileHaveVulnerabilities(logger, securityRiskProfile)) {
                List<LinkableItem> componentAttributes = new LinkedList<>();
                componentAttributes.addAll(commonAttributes);

                Optional<ComponentVersionView> componentVersionView = alertBlackDuckService.getComponentVersion(versionBomComponent);
                if (componentVersionView.isPresent()) {
                    List<LinkableItem> remediationItems = VulnerabilityUtil.getRemediationItems(componentService, componentVersionView.get());
                    componentAttributes.addAll(remediationItems);
                }
                ComponentItem.Builder builder = new ComponentItem.Builder()
                                                    .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_VULNERABILITY)
                                                    .applyOperation(ItemOperation.UPDATE)
                                                    // TODO get the vulnerability id(s) and create a ComponentItem from each of them
                                                    .applyCategoryItem(new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITIES, "Present"))
                                                    .applyCollapseOnCategory(true)
                                                    .applyAllComponentAttributes(componentAttributes)
                                                    .applyNotificationId(notificationId);
                ComponentBuilderUtil.applyComponentInformation(builder, alertBlackDuckService, componentData);
                blackDuckIssueTrackerCallbackUtility.createCallbackInfo(getNotificationType(), versionBomComponent)
                    .ifPresent(builder::applyComponentItemCallbackInfo);
                items.add(builder.build());
            }
        } catch (Exception genericException) {
            logger.warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentName, componentVersionName);
            logger.error("BOM Edit: Error processing vulnerabilities ", genericException);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(AlertBlackDuckService blackDuckResponseCache, BlackDuckApiClient blackDuckApiClient, Long notificationId, ProjectVersionWrapper projectVersionWrapper,
        ProjectVersionComponentView versionBomComponent, List<LinkableItem> commonAttributes) {
        if (!ProjectVersionComponentPolicyStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            // TODO Consider removing this for the sake of issue-trackers.
            //  If a Distribution Job isn't configured for BOM_EDIT, it won't get this anyway.
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().toString();
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
            List<ComponentPolicyRulesView> policyRules = blackDuckApiClient.getAllResponses(versionBomComponent, ProjectVersionComponentView.POLICY_RULES_LINK_RESPONSE);
            for (ComponentPolicyRulesView rule : policyRules) {
                if (!ProjectVersionComponentPolicyStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    // TODO Consider creating a variable for ItemOperation that defaults to UPDATE,
                    //  but set it to INFO if the component is not in violation.
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_NAME, rule.getName(), null);
                LinkableItem policySeverityItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_SEVERITY_NAME, rule.getSeverity().name());
                Optional<ComponentItemCallbackInfo> optionalCallbackInfo = blackDuckIssueTrackerCallbackUtility.createCallbackInfo(getNotificationType(), versionBomComponent);

                List<PolicyRuleExpressionExpressionsView> expressions = rule.getExpression().getExpressions();
                if (policyCommonBuilder.hasVulnerabilityRule(expressions)) {
                    List<ProjectVersionVulnerableBomComponentsView> vulnerableComponentViews = VulnerabilityUtil.getVulnerableComponentViews(blackDuckApiClient, projectVersionWrapper, versionBomComponent);
                    List<ComponentItem> vulnerabilityComponentItems = policyCommonBuilder.createVulnerabilityPolicyComponentItems(
                        vulnerableComponentViews, policyNameItem, policySeverityItem, componentData, optionalCallbackInfo.orElse(null), notificationId, blackDuckApiClient, blackDuckResponseCache);
                    items.addAll(vulnerabilityComponentItems);
                } else {
                    ComponentItem.Builder builder = new ComponentItem.Builder()
                                                        .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                                        .applyOperation(ItemOperation.UPDATE)
                                                        .applyPriority(PolicyPriorityUtil.getPriorityFromSeverity(rule.getSeverity()))
                                                        .applyCategoryItem(policyNameItem)
                                                        .applyAllComponentAttributes(commonAttributes)
                                                        .applyNotificationId(notificationId);
                    ComponentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
                    optionalCallbackInfo.ifPresent(builder::applyComponentItemCallbackInfo);
                    items.add(builder.build());
                }
            }
        } catch (Exception e) {
            logger.error("BOM Edit: Error processing policy ", e);
        }
        return items;
    }

}
