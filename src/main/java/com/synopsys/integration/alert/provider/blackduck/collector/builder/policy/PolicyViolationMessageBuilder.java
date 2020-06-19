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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.datastructure.SetMap;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class PolicyViolationMessageBuilder extends BlackDuckMessageBuilder<RuleViolationNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationMessageBuilder.class);
    private PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public PolicyViolationMessageBuilder(PolicyCommonBuilder policyCommonBuilder) {
        super(NotificationType.RULE_VIOLATION.name());
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, RuleViolationNotificationView notificationView, BlackDuckBucket blackDuckBucket, BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        RuleViolationNotificationContent violationContent = notificationView.getContent();
        ItemOperation operation = ItemOperation.ADD;

        String projectName = violationContent.getProjectName();
        String projectUrl = retrieveNullableProjectUrlAndLog(projectName, blackDuckServicesFactory.createProjectService(), logger::warn);
        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, violationContent.getProjectName(), projectUrl)
                .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, violationContent.getProjectVersionName(), violationContent.getProjectVersion());
            Map<String, PolicyInfo> policyUrlToInfoMap = DataStructureUtils.mapToValues(violationContent.getPolicyInfos(), PolicyInfo::getPolicy);
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = policyCommonBuilder.createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
            FieldAccessor fieldAccessor = commonMessageData.getJob().getFieldAccessor();
            Collection<String> policyFilters = fieldAccessor.getAllStrings(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER);
            List<ComponentItem> items = new LinkedList<>();
            for (Map.Entry<ComponentVersionStatus, Set<PolicyInfo>> componentToPolicyEntry : componentPolicies.entrySet()) {
                ComponentVersionStatus componentVersionStatus = componentToPolicyEntry.getKey();
                Set<PolicyInfo> policies = componentToPolicyEntry.getValue();
                List<ComponentItem> componentItems = retrievePolicyItems(responseCache, blackDuckService, componentService, componentVersionStatus, policies, commonMessageData.getNotificationId(), operation,
                    violationContent.getProjectVersion(),
                    policyFilters);
                items.addAll(componentItems);
            }
            messageContentBuilder.applyAllComponentItems(items);
            return List.of(messageContentBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, ItemOperation operation, String projectVersionUrl, Collection<String> policyFilters) {
        List<ComponentItem> componentItems = new LinkedList<>();
        String componentName = componentVersionStatus.getComponentName();
        String componentVersionName = componentVersionStatus.getComponentVersionName();
        String bomComponentUrl = componentVersionStatus.getBomComponent();

        Optional<ProjectVersionComponentView> optionalBomComponent = blackDuckResponseCache.getBomComponentView(bomComponentUrl);
        ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
        componentItems.addAll(policyCommonBuilder.retrievePolicyItems(blackDuckResponseCache, componentData, policies, notificationId, operation, componentVersionStatus.getBomComponent(), List.of(), policyFilters));
        for (PolicyInfo policyInfo : policies) {
            String policyName = policyInfo.getPolicyName();
            if (policyFilters.isEmpty() || policyFilters.contains(policyName)) {
                LinkableItem policyNameItem = ComponentBuilderUtil.createPolicyNameItem(policyInfo);
                LinkableItem nullablePolicySeverityItem = ComponentBuilderUtil.createPolicySeverityItem(policyInfo).orElse(null);
                Optional<PolicyRuleView> optionalPolicyRule = blackDuckResponseCache.getPolicyRule(blackDuckResponseCache, policyInfo);
                List<PolicyRuleExpressionExpressionsView> expressions = optionalPolicyRule.map(rule -> rule.getExpression().getExpressions()).orElse(List.of());
                if (optionalBomComponent.isPresent() && policyCommonBuilder.hasVulnerabilityRule(expressions)) {
                    List<ComponentItem> vulnerabilityPolicyItems =
                        createVulnerabilityPolicyItems(blackDuckResponseCache, blackDuckService, componentService, optionalBomComponent.get(), policyNameItem, nullablePolicySeverityItem, componentName, componentVersionName,
                            notificationId);
                    componentItems.addAll(vulnerabilityPolicyItems);
                }
            }
        }
        return componentItems;
    }

    private List<ComponentItem> createVulnerabilityPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, ProjectVersionComponentView bomComponent,
        LinkableItem policyNameItem, LinkableItem policySeverity,
        String componentName, String componentVersionName, Long notificationId) {
        List<ComponentItem> vulnerabilityPolicyItems = new ArrayList<>();

        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = blackDuckResponseCache.getProjectVersionWrapper(bomComponent);
        if (optionalProjectVersionWrapper.isPresent()) {
            try {
                ProjectVersionWrapper projectVersionWrapper = optionalProjectVersionWrapper.get();
                String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
                ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
                List<VulnerableComponentView> vulnerableComponentViews = VulnerabilityUtil.getVulnerableComponentViews(blackDuckService, projectVersionWrapper, bomComponent);
                List<ComponentItem> vulnerabilityComponentItems = policyCommonBuilder
                                                                      .createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverity, componentData, notificationId,
                                                                          blackDuckService, blackDuckResponseCache);
                vulnerabilityPolicyItems.addAll(vulnerabilityComponentItems);
                ComponentVersionView componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, bomComponent.getComponentVersion()).orElse(null);

                Optional<ComponentItem> remediationComponentItem = createRemediationComponentItem(blackDuckResponseCache, MessageBuilderConstants.CATEGORY_TYPE_POLICY, componentService, componentVersionView, componentData, policyNameItem,
                    policySeverity, true, notificationId);
                remediationComponentItem.ifPresent(vulnerabilityPolicyItems::add);
            } catch (IntegrationException e) {
                logger.debug(String.format("Could not get the project/version. Skipping vulnerability info for this policy: %s. Exception: %s", policyNameItem, e.getMessage()), e);
            }
        }
        return vulnerabilityPolicyItems;
    }

    protected Optional<ComponentItem> createRemediationComponentItem(BlackDuckResponseCache blackDuckResponseCache, String categoryType, ComponentService componentService, ComponentVersionView componentVersionView,
        ComponentData componentData, LinkableItem categoryItem, LinkableItem categoryGrouping, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = VulnerabilityUtil.getRemediationItems(componentService, componentVersionView);
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
                ComponentBuilderUtil.applyComponentInformation(remediationComponent, blackDuckResponseCache, componentData);

                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

}
