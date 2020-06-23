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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.BlackDuckIssueTrackerCallbackUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.VulnerabilityWithRemediationSeverityType;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class PolicyCommonBuilder {
    private final Logger logger = LoggerFactory.getLogger(PolicyCommonBuilder.class);

    public List<ComponentItem> retrievePolicyItems(NotificationType notificationType, BlackDuckResponseCache blackDuckResponseCache, ComponentData componentData,
        Collection<PolicyInfo> policies, Long notificationId, ItemOperation operation, String bomComponentUrl, List<LinkableItem> customAttributes, Collection<String> policyFilter) {
        List<ComponentItem> componentItems = new LinkedList<>();
        for (PolicyInfo policyInfo : policies) {
            String policyName = policyInfo.getPolicyName();
            if (policyFilter.isEmpty() || policyFilter.contains(policyName)) {
                ComponentItemPriority priority = PolicyPriorityUtil.getPriorityFromSeverity(policyInfo.getSeverity());

                LinkableItem policyNameItem = ComponentBuilderUtil.createPolicyNameItem(policyInfo);
                LinkableItem nullablePolicySeverityItem = ComponentBuilderUtil.createPolicySeverityItem(policyInfo).orElse(null);
                ComponentItemCallbackInfo nullableCallbackInfo = null;

                List<LinkableItem> policyAttributes = new ArrayList<>();
                Optional<ProjectVersionComponentView> optionalBomComponent = blackDuckResponseCache.getBomComponentView(bomComponentUrl);
                if (optionalBomComponent.isPresent()) {
                    ProjectVersionComponentView bomComponent = optionalBomComponent.get();
                    nullableCallbackInfo = BlackDuckIssueTrackerCallbackUtil.createCallbackInfo(notificationType, bomComponent).orElse(null);
                    policyAttributes.addAll(ComponentBuilderUtil.getLicenseLinkableItems(bomComponent));
                    policyAttributes.addAll(ComponentBuilderUtil.getUsageLinkableItems(bomComponent));
                }
                policyAttributes.addAll(customAttributes);

                try {
                    ComponentItem.Builder builder = new ComponentItem.Builder()
                                                        .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                                        .applyOperation(operation)
                                                        .applyPriority(priority)
                                                        .applyCategoryItem(policyNameItem)
                                                        .applyComponentItemCallbackInfo(nullableCallbackInfo)
                                                        .applyCategoryGroupingAttribute(nullablePolicySeverityItem)
                                                        .applyAllComponentAttributes(policyAttributes)
                                                        .applyNotificationId(notificationId);
                    ComponentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
                    componentItems.add(builder.build());
                } catch (Exception ex) {
                    logger.info("Error building policy component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentData.getComponentName(), componentData.getComponentVersionName());
                    logger.debug("Error building policy component cause ", ex);
                }
            }
        }
        return componentItems;
    }

    public SetMap<ComponentVersionStatus, PolicyInfo> createComponentToPolicyMapping(
        Collection<ComponentVersionStatus> componentVersionStatuses, Map<String, PolicyInfo> policyItems) {
        SetMap<ComponentVersionStatus, PolicyInfo> componentToPolicyMapping = SetMap.createDefault();
        for (ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            Set<PolicyInfo> componentPolicies = getPoliciesForComponent(componentVersionStatus, policyItems);
            componentToPolicyMapping.addAll(componentVersionStatus, componentPolicies);
        }
        return componentToPolicyMapping;
    }

    public Set<PolicyInfo> getPoliciesForComponent(ComponentVersionStatus componentVersionStatus, Map<String, PolicyInfo> policyItems) {
        return componentVersionStatus.getPolicies().stream()
                   .filter(policyItems::containsKey)
                   .map(policyItems::get)
                   .collect(Collectors.toSet());
    }

    public boolean hasVulnerabilityRule(List<PolicyRuleExpressionExpressionsView> expressions) {
        for (PolicyRuleExpressionExpressionsView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(MessageBuilderConstants.VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public List<ComponentItem> createVulnerabilityPolicyComponentItems(Collection<VulnerableComponentView> vulnerableComponentViews, LinkableItem policyNameItem, LinkableItem policySeverity,
        ComponentData componentData, Long notificationId, BlackDuckService blackDuckService, BlackDuckResponseCache blackDuckResponseCache) {
        Map<String, VulnerabilityView> vulnerabilityViews = VulnerabilityUtil.createVulnerabilityViewMap(logger, blackDuckService, vulnerableComponentViews);
        List<VulnerabilityWithRemediationView> notificationVulnerabilities = vulnerableComponentViews.stream()
                                                                                 .map(VulnerableComponentView::getVulnerabilityWithRemediation)
                                                                                 .sorted(Comparator.comparing(VulnerabilityWithRemediationView::getSeverity))
                                                                                 .collect(Collectors.toList());
        SetMap<LinkableItem, LinkableItem> severityToVulns = SetMap.createLinked();
        for (VulnerabilityWithRemediationView vulnerabilityView : notificationVulnerabilities) {
            // TODO to get the URLS for vulnerabilities we would want to traverse the vulnerabilities link
            String vulnerabilityId = vulnerabilityView.getVulnerabilityName();
            String vulnerabilityUrl = null;
            if (vulnerabilityViews.containsKey(vulnerabilityId)) {
                vulnerabilityUrl = vulnerabilityViews.get(vulnerabilityId).getHref().orElse(null);
            }
            LinkableItem vulnerabilityIdItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITIES, vulnerabilityId, vulnerabilityUrl);
            vulnerabilityIdItem.setCollapsible(true);

            String severity = Optional.ofNullable(vulnerabilityView.getSeverity())
                                  .map(VulnerabilityWithRemediationSeverityType::name)
                                  .orElse("UNKNOWN");
            LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITY_SEVERITY, severity);

            severityToVulns.add(severityItem, vulnerabilityIdItem);
        }

        List<ComponentItem> vulnerabilityItems = new ArrayList<>();
        for (Map.Entry<LinkableItem, Set<LinkableItem>> groupedVulnEntries : severityToVulns.entrySet()) {
            String severityValue = groupedVulnEntries.getKey().getValue();
            ComponentItemPriority priority = ComponentItemPriority.findPriority(severityValue);

            List<LinkableItem> vulnAttributes = groupedVulnEntries.getValue()
                                                    .stream()
                                                    .map(vulnItem -> VulnerabilityUtil.createVulnerabilityAttributeItem(severityValue, vulnItem))
                                                    .collect(Collectors.toList());
            ComponentItem.Builder builder = new ComponentItem.Builder()
                                                .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                                .applyOperation(ItemOperation.INFO)
                                                .applyPriority(priority)
                                                .applyCategoryItem(policyNameItem)
                                                .applyCategoryGroupingAttribute(policySeverity)
                                                .applyCollapseOnCategory(false)
                                                .applyAllComponentAttributes(vulnAttributes)
                                                .applyNotificationId(notificationId);
            ComponentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
            try {
                vulnerabilityItems.add(builder.build());
            } catch (AlertException ex) {
                logger.info("Error building policy bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentData.getComponentName(),
                    componentData.getComponentVersionName());
                logger.error("Error building policy bom edit component cause ", ex);
            }
        }

        return vulnerabilityItems;
    }
}
