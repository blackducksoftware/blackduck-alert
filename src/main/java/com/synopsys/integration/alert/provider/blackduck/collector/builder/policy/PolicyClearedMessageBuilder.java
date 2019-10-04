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
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

@Component
public class PolicyClearedMessageBuilder implements BlackDuckMessageBuilder<RuleViolationClearedNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyClearedMessageBuilder.class);
    private PolicyPriorityUtil policyPriorityUtil;
    private ComponentBuilderUtil componentBuilderUtil;
    private PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public PolicyClearedMessageBuilder(PolicyPriorityUtil policyPriorityUtil, ComponentBuilderUtil componentBuilderUtil, PolicyCommonBuilder policyCommonBuilder) {
        this.policyPriorityUtil = policyPriorityUtil;
        this.componentBuilderUtil = componentBuilderUtil;
        this.policyCommonBuilder = policyCommonBuilder;
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
        RuleViolationClearedNotificationContent violationContent = notificationView.getContent();
        ItemOperation operation = ItemOperation.DELETE;
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
                final List<ComponentItem> componentItems = retrievePolicyItems(responseCache, componentVersionStatus, policies, notificationId, operation, violationContent.getProjectVersion());
                items.addAll(componentItems);
            }
            projectVersionMessageBuilder.applyAllComponentItems(items);
            return List.of(projectVersionMessageBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation cleared message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, ItemOperation operation, String projectVersionUrl) {
        List<ComponentItem> componentItems = new LinkedList<>();
        String componentName = componentVersionStatus.getComponentName();
        String componentVersionName = componentVersionStatus.getComponentVersionName();

        componentItems.addAll(policyCommonBuilder.retrievePolicyItems(blackDuckResponseCache, componentName, componentVersionName, policies, notificationId, operation, projectVersionUrl,
            componentVersionStatus.getBomComponent(), List.of()));
        for (PolicyInfo policyInfo : policies) {
            LinkableItem policyNameItem = componentBuilderUtil.createPolicyNameItem(policyInfo);
            Optional<PolicyRuleView> optionalPolicyRule = blackDuckResponseCache.getPolicyRule(blackDuckResponseCache, policyInfo);
            if (optionalPolicyRule.isPresent() && policyCommonBuilder.hasVulnerabilityRule(optionalPolicyRule.get())) {
                Optional<ComponentItem> vulnerabilityComponent = createEmptyVulnerabilityItem(blackDuckResponseCache, policyNameItem, componentName, componentVersionName, projectVersionUrl, notificationId, operation);
                vulnerabilityComponent.ifPresent(componentItems::add);
            }
        }
        return componentItems;
    }

    private Optional<ComponentItem> createEmptyVulnerabilityItem(BlackDuckResponseCache blackDuckResponseCache, LinkableItem policyNameItem, String componentName, String componentVersionName, String projectVersionUrl, Long notificationId,
        ItemOperation operation) {
        LinkableItem vulnerabilityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITIES, "ALL", null);
        LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITY_SEVERITY, ComponentItemPriority.NONE.name());
        ComponentItemPriority priority = ComponentItemPriority.findPriority(severityItem.getValue());

        ComponentItem.Builder builder = new ComponentItem.Builder()
                                            .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                            .applyOperation(operation)
                                            .applyPriority(priority)
                                            .applyCategoryItem(policyNameItem)
                                            .applyCategoryGroupingAttribute(severityItem)
                                            .applyAllComponentAttributes(Set.of(vulnerabilityItem))
                                            .applyNotificationId(notificationId);
        componentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentName, componentVersionName, projectVersionUrl);
        try {
            return Optional.of(builder.build());
        } catch (AlertException ex) {
            logger.info("Error building policy vulnerability component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentName, componentVersionName);
            logger.error("Error building policy vulnerability component cause ", ex);
        }
        return Optional.empty();
    }

}
