/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

@Component
public class PolicyClearedMessageBuilder implements BlackDuckMessageBuilder<RuleViolationClearedNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyClearedMessageBuilder.class);
    private PolicyPriorityUtil policyPriorityUtil;

    @Autowired
    public PolicyClearedMessageBuilder(PolicyPriorityUtil policyPriorityUtil) {
        this.policyPriorityUtil = policyPriorityUtil;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.RULE_VIOLATION_CLEARED.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, RuleViolationClearedNotificationView notificationView,
        BlackDuckBucket blackDuckBucket, BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        RuleViolationClearedNotificationContent violationContent = notificationView.getContent();
        ItemOperation operation = ItemOperation.DELETE;
        try {
            ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                              .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                              .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, violationContent.getProjectName())
                                                                              .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, violationContent.getProjectVersionName(), violationContent.getProjectVersion())
                                                                              .applyProviderCreationTime(providerCreationDate);
            Map<String, PolicyInfo> policyUrlToInfoMap = violationContent.getPolicyInfos().stream().collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
            List<ComponentItem> items = new LinkedList<>();
            for (Map.Entry<ComponentVersionStatus, Set<PolicyInfo>> componentToPolicyEntry : componentPolicies.entrySet()) {
                ComponentVersionStatus componentVersionStatus = componentToPolicyEntry.getKey();
                Set<PolicyInfo> policies = componentToPolicyEntry.getValue();
                final List<ComponentItem> componentItems = retrievePolicyItems(responseCache, blackDuckService, componentService, componentVersionStatus, policies, notificationId, operation, violationContent.getProjectVersion());
                items.addAll(componentItems);
            }
            projectVersionMessageBuilder.applyAllComponentItems(items);
            return List.of(projectVersionMessageBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation cleared message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, ItemOperation operation, String projectVersionUrl) {
        List<ComponentItem> componentItems = new LinkedList<>();
        for (PolicyInfo policyInfo : policies) {
            ComponentItemPriority priority = policyPriorityUtil.getPriorityFromSeverity(policyInfo.getSeverity());

            String bomComponentUrl = null;
            if (null != componentVersionStatus) {
                bomComponentUrl = componentVersionStatus.getBomComponent();
            }

            Optional<VersionBomComponentView> optionalBomComponent = blackDuckResponseCache.getBomComponentView(bomComponentUrl);

            List<LinkableItem> policyAttributes = new ArrayList<>();
            LinkableItem policyNameItem = createPolicyNameItem(policyInfo);
            LinkableItem nullablePolicySeverityItem = createPolicySeverityItem(policyInfo).orElse(null);
            optionalBomComponent.ifPresent(bomComponent -> {
                policyAttributes.addAll(getLicenseLinkableItems(bomComponent));
                policyAttributes.addAll(getUsageLinkableItems(bomComponent));
            });

            String componentName = componentVersionStatus.getComponentName();
            String componentVersionName = componentVersionStatus.getComponentVersionName();
            String projectQueryLink = blackDuckResponseCache.getProjectComponentQueryLink(projectVersionUrl, ProjectVersionView.VULNERABLE_COMPONENTS_LINK, componentName).orElse(null);
            LinkableItem componentItem;
            LinkableItem componentVersionItem = null;
            if (StringUtils.isNotBlank(componentVersionName)) {
                componentVersionItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_VERSION_NAME, componentVersionName, projectQueryLink);
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName);
            } else {
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName, projectQueryLink);
            }

            try {
                ComponentItem.Builder builder = new ComponentItem.Builder()
                                                    .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                                    .applyOperation(operation)
                                                    .applyPriority(priority)
                                                    .applyComponentData(componentItem)
                                                    .applySubComponent(componentVersionItem)
                                                    .applyCategoryItem(policyNameItem)
                                                    .applyCategoryGroupingAttribute(nullablePolicySeverityItem)
                                                    .applyAllComponentAttributes(policyAttributes)
                                                    .applyNotificationId(notificationId);
                componentItems.add(builder.build());
            } catch (Exception ex) {
                logger.info("Error building policy component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentItem, componentVersionItem);
                logger.error("Error building policy component cause ", ex);
            }
            Optional<PolicyRuleView> optionalPolicyRule = getPolicyRule(blackDuckResponseCache, policyInfo);
            if (optionalPolicyRule.isPresent() && hasVulnerabilityRule(optionalPolicyRule.get())) {
                Optional<ComponentItem> vulnerabilityComponent = createEmptyVulnerabilityItem(policyNameItem, componentItem, componentVersionItem, notificationId, operation);
                vulnerabilityComponent.ifPresent(componentItems::add);
            }
        }
        return componentItems;
    }

    protected LinkableItem createPolicyNameItem(PolicyInfo policyInfo) {
        String policyName = policyInfo.getPolicyName();
        return new LinkableItem(MessageBuilderConstants.LABEL_POLICY_NAME, policyName);
    }

    protected Optional<LinkableItem> createPolicySeverityItem(PolicyInfo policyInfo) {
        String severity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(severity)) {
            LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_SEVERITY_NAME, severity);
            return Optional.of(severityItem);
        }
        return Optional.empty();
    }

    public boolean hasVulnerabilityRule(PolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(MessageBuilderConstants.VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public Optional<PolicyRuleView> getPolicyRule(BlackDuckResponseCache blackDuckResponseCache, PolicyInfo policyInfo) {
        try {
            String policyUrl = policyInfo.getPolicy();
            if (StringUtils.isNotBlank(policyUrl)) {
                return blackDuckResponseCache.getItem(PolicyRuleView.class, policyUrl);
            }
        } catch (Exception e) {
            logger.debug("Unable to get policy rule: {}", policyInfo.getPolicyName());
            logger.debug("Cause:", e);
        }
        return Optional.empty();
    }

    public List<LinkableItem> getLicenseLinkableItems(VersionBomComponentView bomComponentView) {
        return bomComponentView.getLicenses()
                   .stream()
                   .map(licenseView -> new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_LICENSE, licenseView.getLicenseDisplay()))
                   .collect(Collectors.toList());
    }

    public List<LinkableItem> getUsageLinkableItems(VersionBomComponentView bomComponentView) {
        return bomComponentView.getUsages()
                   .stream()
                   .map(MatchedFileUsagesType::prettyPrint)
                   .map(usage -> new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_USAGE, usage))
                   .collect(Collectors.toList());
    }

    private SetMap<ComponentVersionStatus, PolicyInfo> createComponentToPolicyMapping(
        Collection<ComponentVersionStatus> componentVersionStatuses, Map<String, PolicyInfo> policyItems) {
        SetMap<ComponentVersionStatus, PolicyInfo> componentToPolicyMapping = SetMap.createDefault();
        for (ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            Set<PolicyInfo> componentPolicies = getPoliciesForComponent(componentVersionStatus, policyItems);
            componentToPolicyMapping.addAll(componentVersionStatus, componentPolicies);
        }
        return componentToPolicyMapping;
    }

    private Set<PolicyInfo> getPoliciesForComponent(ComponentVersionStatus componentVersionStatus, Map<String, PolicyInfo> policyItems) {
        return componentVersionStatus.getPolicies().stream()
                   .filter(policyItems::containsKey)
                   .map(policyItems::get)
                   .collect(Collectors.toSet());
    }

    private Optional<ComponentItem> createEmptyVulnerabilityItem(LinkableItem policyNameItem, LinkableItem componentItem, LinkableItem componentVersionItem, Long notificationId, ItemOperation operation) {
        LinkableItem vulnerabilityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITIES, "ALL", null);
        LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITY_SEVERITY, ComponentItemPriority.NONE.name());
        ComponentItemPriority priority = ComponentItemPriority.findPriority(severityItem.getValue());

        ComponentItem.Builder builder = new ComponentItem.Builder()
                                            .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                            .applyOperation(operation)
                                            .applyPriority(priority)
                                            .applyComponentData(componentItem)
                                            .applySubComponent(componentVersionItem)
                                            .applyCategoryItem(policyNameItem)
                                            .applyCategoryGroupingAttribute(severityItem)
                                            .applyAllComponentAttributes(Set.of(vulnerabilityItem))
                                            .applyNotificationId(notificationId);
        try {
            return Optional.of(builder.build());
        } catch (AlertException ex) {
            logger.info("Error building policy vulnerability component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentItem, componentVersionItem);
            logger.error("Error building policy vulnerability component cause ", ex);
        }
        return Optional.empty();
    }

}
