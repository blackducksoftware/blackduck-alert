/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.BlackDuckIssueTrackerCallbackUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Component
public class BomEditMessageBuilder extends BlackDuckMessageBuilder<BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(BomEditMessageBuilder.class);
    private final PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public BomEditMessageBuilder(PolicyCommonBuilder policyCommonBuilder) {
        super(NotificationType.BOM_EDIT);
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, BomEditNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BomEditNotificationContent bomEditContent = notificationView.getContent();
        Optional<ProjectVersionComponentView> optionalBomComponent = responseCache.getBomComponentView(bomEditContent.getBomComponent());
        Long notificationId = commonMessageData.getNotificationId();

        // TODO Stop using this when Black Duck supports going back to the project-version
        // blackduck-common-api 2019.12.0.4+ should have the project version link in the BomEditNotificationContent
        Optional<ProjectVersionWrapper> projectVersionWrapper = optionalBomComponent.flatMap(responseCache::getProjectVersionWrapper);

        if (optionalBomComponent.isPresent() && projectVersionWrapper.isPresent()) {
            ProjectVersionComponentView bomComponent = optionalBomComponent.get();
            try {
                ProjectVersionWrapper projectVersionData = projectVersionWrapper.get();
                ProjectView projectView = projectVersionData.getProjectView();
                ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();

                messageContentBuilder
                    .applyCommonData(commonMessageData)
                    .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, projectView.getName(), projectView.getHref().orElse(null))
                    .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, projectVersionData.getProjectVersionView().getVersionName(),
                        projectVersionData.getProjectVersionView().getHref().orElse(null));

                List<LinkableItem> commonAttributes = Stream.concat(ComponentBuilderUtil.getLicenseLinkableItems(bomComponent).stream(), ComponentBuilderUtil.getUsageLinkableItems(bomComponent).stream())
                                                          .collect(Collectors.toList());

                List<ComponentItem> componentItems = new LinkedList<>(addVulnerabilityData(responseCache, componentService, notificationId, bomComponent, projectVersionData, commonAttributes));
                projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(createPolicyItems(responseCache, blackDuckService, notificationId, versionWrapper, bomComponent, commonAttributes)));

                messageContentBuilder.applyAllComponentItems(componentItems);
                return List.of(messageContentBuilder.build());
            } catch (AlertException ex) {
                logger.error("Error creating policy violation message.", ex);
            }
        }
        return List.of();
    }

    private Collection<ComponentItem> addVulnerabilityData(BlackDuckResponseCache blackDuckResponseCache, ComponentService componentService, Long notificationId, ProjectVersionComponentView versionBomComponent,
        ProjectVersionWrapper projectVersionWrapper, List<LinkableItem> commonAttributes) {
        Collection<ComponentItem> items = new LinkedList<>();
        RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
        String componentName = versionBomComponent.getComponentName();
        String componentVersionName = versionBomComponent.getComponentVersionName();
        String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
        try {
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.VULNERABLE_COMPONENTS_LINK);
            if (VulnerabilityUtil.doesSecurityRiskProfileHaveVulnerabilities(logger, securityRiskProfile)) {
                List<LinkableItem> componentAttributes = new LinkedList<>();
                componentAttributes.addAll(commonAttributes);

                Optional<ComponentVersionView> componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, versionBomComponent.getComponentVersion());
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
                ComponentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
                BlackDuckIssueTrackerCallbackUtil.createCallbackInfo(getNotificationType(), versionBomComponent)
                    .ifPresent(builder::applyComponentItemCallbackInfo);
                items.add(builder.build());
            }
        } catch (Exception genericException) {
            logger.warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentName, componentVersionName);
            logger.error("BOM Edit: Error processing vulnerabilities ", genericException);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, Long notificationId, ProjectVersionWrapper projectVersionWrapper,
        ProjectVersionComponentView versionBomComponent, List<LinkableItem> commonAttributes) {
        if (!PolicyStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
            List<ComponentPolicyRulesView> policyRules = blackDuckService.getAllResponses(versionBomComponent, ProjectVersionComponentView.POLICY_RULES_LINK_RESPONSE);
            for (ComponentPolicyRulesView rule : policyRules) {
                if (!PolicyStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_NAME, rule.getName(), null);
                LinkableItem policySeverityItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_SEVERITY_NAME, rule.getSeverity().name());
                List<PolicyRuleExpressionExpressionsView> expressions = rule.getExpression().getExpressions();
                if (policyCommonBuilder.hasVulnerabilityRule(expressions)) {
                    List<VulnerableComponentView> vulnerableComponentViews = VulnerabilityUtil.getVulnerableComponentViews(blackDuckService, projectVersionWrapper, versionBomComponent);
                    List<ComponentItem> vulnerabilityComponentItems =
                        policyCommonBuilder.createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverityItem, componentData, notificationId, blackDuckService,
                            blackDuckResponseCache);
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
                    BlackDuckIssueTrackerCallbackUtil.createCallbackInfo(getNotificationType(), versionBomComponent)
                        .ifPresent(builder::applyComponentItemCallbackInfo);
                    items.add(builder.build());
                }
            }
        } catch (Exception e) {
            logger.error("BOM Edit: Error processing policy ", e);
        }

        return items;
    }
}
