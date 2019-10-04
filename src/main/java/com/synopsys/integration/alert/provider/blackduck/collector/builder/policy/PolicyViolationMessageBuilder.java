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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class PolicyViolationMessageBuilder implements BlackDuckMessageBuilder<RuleViolationNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationMessageBuilder.class);
    private PolicyPriorityUtil policyPriorityUtil;
    private VulnerabilityUtil vulnerabilityUtil;
    private ComponentBuilderUtil componentBuilderUtil;
    private PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public PolicyViolationMessageBuilder(PolicyPriorityUtil policyPriorityUtil, VulnerabilityUtil vulnerabilityUtil, ComponentBuilderUtil componentBuilderUtil, PolicyCommonBuilder policyCommonBuilder) {
        this.policyPriorityUtil = policyPriorityUtil;
        this.vulnerabilityUtil = vulnerabilityUtil;
        this.componentBuilderUtil = componentBuilderUtil;
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.RULE_VIOLATION.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, RuleViolationNotificationView notificationView,
        BlackDuckBucket blackDuckBucket, BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        RuleViolationNotificationContent violationContent = notificationView.getContent();
        ItemOperation operation = ItemOperation.ADD;
        try {
            ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                              .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                              .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, violationContent.getProjectName())
                                                                              .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, violationContent.getProjectVersionName(), violationContent.getProjectVersion())
                                                                              .applyProviderCreationTime(providerCreationDate);
            Map<String, PolicyInfo> policyUrlToInfoMap = violationContent.getPolicyInfos().stream().collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = policyCommonBuilder.createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
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
            logger.error("Error creating policy violation message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, ItemOperation operation, String projectVersionUrl) {
        List<ComponentItem> componentItems = new LinkedList<>();
        String componentName = componentVersionStatus.getComponentName();
        String componentVersionName = componentVersionStatus.getComponentVersionName();
        String bomComponentUrl = null;
        if (null != componentVersionStatus) {
            bomComponentUrl = componentVersionStatus.getBomComponent();
        }
        Optional<VersionBomComponentView> optionalBomComponent = blackDuckResponseCache.getBomComponentView(bomComponentUrl);

        componentItems.addAll(policyCommonBuilder.retrievePolicyItems(blackDuckResponseCache, componentName, componentVersionName, policies, notificationId, operation, projectVersionUrl,
            componentVersionStatus.getBomComponent(), List.of()));
        for (PolicyInfo policyInfo : policies) {
            LinkableItem policyNameItem = componentBuilderUtil.createPolicyNameItem(policyInfo);
            LinkableItem nullablePolicySeverityItem = componentBuilderUtil.createPolicySeverityItem(policyInfo).orElse(null);
            Optional<PolicyRuleView> optionalPolicyRule = blackDuckResponseCache.getPolicyRule(blackDuckResponseCache, policyInfo);
            if (optionalPolicyRule.isPresent() && policyCommonBuilder.hasVulnerabilityRule(optionalPolicyRule.get())) {
                if (optionalBomComponent.isPresent()) {
                    List<ComponentItem> vulnerabilityPolicyItems =
                        createVulnerabilityPolicyItems(blackDuckResponseCache, blackDuckService, componentService, optionalBomComponent.get(), policyNameItem, nullablePolicySeverityItem, componentName, componentVersionName,
                            notificationId);
                    componentItems.addAll(vulnerabilityPolicyItems);
                }
            }
        }
        return componentItems;
    }

    private List<ComponentItem> createVulnerabilityPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, VersionBomComponentView bomComponent,
        LinkableItem policyNameItem, LinkableItem policySeverity,
        String componentName, String componentVersionName, Long notificationId) {
        List<ComponentItem> vulnerabilityPolicyItems = new ArrayList<>();
        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = blackDuckResponseCache.getProjectVersionWrapper(bomComponent);
        if (optionalProjectVersionWrapper.isPresent()) {
            try {
                ProjectVersionWrapper projectVersionWrapper = optionalProjectVersionWrapper.get();
                String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
                List<VulnerableComponentView> vulnerableComponentViews = vulnerabilityUtil.getVulnerableComponentViews(blackDuckService, projectVersionWrapper, bomComponent);
                List<ComponentItem> vulnerabilityComponentItems = policyCommonBuilder
                                                                      .createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverity, componentName, componentVersionName, projectVersionUrl, notificationId,
                                                                          blackDuckService, blackDuckResponseCache);
                vulnerabilityPolicyItems.addAll(vulnerabilityComponentItems);

                // TODO: remove the orElse null.
                ComponentVersionView componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, bomComponent.getComponentVersion()).orElse(null);

                Optional<ComponentItem> remediationComponentItem = createRemediationComponentItem(blackDuckResponseCache, MessageBuilderConstants.CATEGORY_TYPE_POLICY, componentService, componentVersionView, projectVersionUrl,
                    componentName, componentVersionName, policyNameItem,
                    policySeverity, true,
                    notificationId);
                remediationComponentItem.ifPresent(vulnerabilityPolicyItems::add);
            } catch (IntegrationException e) {
                logger.debug("Could not get the project/version. Skipping vulnerability info for this policy: {}. Exception: {}", policyNameItem, e);
            }
        }
        return vulnerabilityPolicyItems;
    }

    protected Optional<ComponentItem> createRemediationComponentItem(BlackDuckResponseCache blackDuckResponseCache, String categoryType, ComponentService componentService, ComponentVersionView componentVersionView, String componentName,
        String componentVersionName, String projectVersionURL, LinkableItem categoryItem, LinkableItem categoryGrouping, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = vulnerabilityUtil.getRemediationItems(componentService, componentVersionView);
            if (!remediationItems.isEmpty()) {
                ComponentItem.Builder remediationComponent = new ComponentItem.Builder()
                                                                 .applyCategory(categoryType)
                                                                 .applyOperation(ItemOperation.INFO)
                                                                 .applyPriority(ComponentItemPriority.NONE)
                                                                 .applyCategoryItem(categoryItem)
                                                                 .applyCategoryGroupingAttribute(categoryGrouping)
                                                                 .applyCollapseOnCategory(collapseOnCategory)
                                                                 .applyAllComponentAttributes(remediationItems)
                                                                 .applyNotificationId(notificationId);
                componentBuilderUtil.applyComponentInformation(remediationComponent, blackDuckResponseCache, componentName, componentVersionName, projectVersionURL);

                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

}
