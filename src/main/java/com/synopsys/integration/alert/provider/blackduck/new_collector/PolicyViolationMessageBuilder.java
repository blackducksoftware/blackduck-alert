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
package com.synopsys.integration.alert.provider.blackduck.new_collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.BlackDuckResponseCache;
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.VulnerabilityUtil;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
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
    public static final String CATEGORY_TYPE = "Policy";
    public static final String VULNERABILITY_CHECK_TEXT = "vuln";
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationMessageBuilder.class);
    private final Map<String, ComponentItemPriority> policyPriorityMap = new HashMap<>();
    private VulnerabilityUtil vulnerabilityUtil;

    @Autowired
    public PolicyViolationMessageBuilder(VulnerabilityUtil vulnerabilityUtil) {
        this.vulnerabilityUtil = vulnerabilityUtil;
        policyPriorityMap.put("blocker", ComponentItemPriority.HIGHEST);
        policyPriorityMap.put("critical", ComponentItemPriority.HIGH);
        policyPriorityMap.put("major", ComponentItemPriority.MEDIUM);
        policyPriorityMap.put("minor", ComponentItemPriority.LOW);
        policyPriorityMap.put("trivial", ComponentItemPriority.LOWEST);
        policyPriorityMap.put("unspecified", ComponentItemPriority.NONE);
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
            logger.error("Error creating policy violation message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, ItemOperation operation, String projectVersionUrl) {
        List<ComponentItem> componentItems = new LinkedList<>();
        for (PolicyInfo policyInfo : policies) {
            ComponentItemPriority priority = getPolicyPriority(policyInfo.getSeverity());

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
                                                    .applyCategory(CATEGORY_TYPE)
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
                if (optionalBomComponent.isPresent()) {
                    List<ComponentItem> vulnerabilityPolicyItems =
                        createVulnerabilityPolicyItems(blackDuckResponseCache, blackDuckService, componentService, optionalBomComponent.get(), policyNameItem, nullablePolicySeverityItem, componentItem, componentVersionItem,
                            notificationId);
                    componentItems.addAll(vulnerabilityPolicyItems);
                }
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
            if (expressionView.getName().toLowerCase().contains(VULNERABILITY_CHECK_TEXT)) {
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

    protected ComponentItemPriority getPolicyPriority(String severity) {
        if (StringUtils.isNotBlank(severity)) {
            String severityKey = severity.trim().toLowerCase();
            return policyPriorityMap.getOrDefault(severityKey, ComponentItemPriority.NONE);
        }
        return ComponentItemPriority.NONE;
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

    private List<ComponentItem> createVulnerabilityPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, VersionBomComponentView bomComponent,
        LinkableItem policyNameItem, LinkableItem policySeverity,
        LinkableItem componentItem, LinkableItem componentVersionItem, Long notificationId) {
        List<ComponentItem> vulnerabilityPolicyItems = new ArrayList<>();
        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = getProjectVersionWrapper(blackDuckResponseCache, bomComponent);
        if (optionalProjectVersionWrapper.isPresent()) {
            try {
                List<VulnerableComponentView> vulnerableComponentViews = vulnerabilityUtil.getVulnerableComponentViews(blackDuckService, optionalProjectVersionWrapper.get(), bomComponent);
                List<ComponentItem> vulnerabilityComponentItems =
                    createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverity, componentItem, componentVersionItem, notificationId, blackDuckService, blackDuckResponseCache);
                vulnerabilityPolicyItems.addAll(vulnerabilityComponentItems);

                // TODO: remove the orElse null.
                ComponentVersionView componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, bomComponent.getComponentVersion()).orElse(null);

                Optional<ComponentItem> remediationComponentItem = createRemediationComponentItem(CATEGORY_TYPE, componentService, componentVersionView, componentItem, componentVersionItem, policyNameItem, policySeverity, true,
                    notificationId);
                remediationComponentItem.ifPresent(vulnerabilityPolicyItems::add);
            } catch (IntegrationException e) {
                logger.debug("Could not get the project/version. Skipping vulnerability info for this policy: {}. Exception: {}", policyNameItem, e);
            }
        }
        return vulnerabilityPolicyItems;
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(BlackDuckResponseCache blackDuckResponseCache, VersionBomComponentView versionBomComponent) {
        // TODO Stop using this when Black Duck supports going back to the project-version
        final Optional<String> versionBomComponentHref = versionBomComponent.getHref();
        if (versionBomComponentHref.isPresent()) {
            String versionHref = versionBomComponentHref.get();
            int componentsIndex = versionHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
            String projectVersionUri = versionHref.substring(0, componentsIndex - 1);

            Optional<ProjectVersionView> projectVersion = blackDuckResponseCache.getItem(ProjectVersionView.class, projectVersionUri);
            ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
            projectVersion.ifPresent(wrapper::setProjectVersionView);
            projectVersion.flatMap(version -> blackDuckResponseCache.getItem(ProjectView.class, version.getFirstLink(ProjectVersionView.PROJECT_LINK).orElse("")))
                .ifPresent(wrapper::setProjectView);
            return Optional.of(wrapper);

        }

        return Optional.empty();
    }

    protected Optional<ComponentItem> createRemediationComponentItem(String categoryType, ComponentService componentService, ComponentVersionView componentVersionView, LinkableItem componentItem, LinkableItem componentVersionItem,
        LinkableItem categoryItem, LinkableItem categoryGrouping, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = vulnerabilityUtil.getRemediationItems(componentService, componentVersionView);
            if (!remediationItems.isEmpty()) {
                ComponentItem.Builder remediationComponent = new ComponentItem.Builder()
                                                                 .applyCategory(categoryType)
                                                                 .applyOperation(ItemOperation.INFO)
                                                                 .applyPriority(ComponentItemPriority.NONE)
                                                                 .applyComponentData(componentItem)
                                                                 .applySubComponent(componentVersionItem)
                                                                 .applyCategoryItem(categoryItem)
                                                                 .applyCategoryGroupingAttribute(categoryGrouping)
                                                                 .applyCollapseOnCategory(collapseOnCategory)
                                                                 .applyAllComponentAttributes(remediationItems)
                                                                 .applyNotificationId(notificationId);

                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

    protected List<ComponentItem> createVulnerabilityPolicyComponentItems(Collection<VulnerableComponentView> vulnerableComponentViews, LinkableItem policyNameItem, LinkableItem policySeverity,
        LinkableItem componentItem, LinkableItem componentVersionItem, Long notificationId, BlackDuckService blackDuckService, BlackDuckResponseCache blackDuckResponseCache) {
        Map<String, VulnerabilityView> vulnerabilityViews = createVulnerabilityViewMap(blackDuckService, vulnerableComponentViews);
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
            LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITY_SEVERITY, blackDuckResponseCache.getSeverity(vulnerabilityUrl));

            severityToVulns.add(severityItem, vulnerabilityIdItem);
        }

        List<ComponentItem> vulnerabilityItems = new ArrayList<>();
        for (Map.Entry<LinkableItem, Set<LinkableItem>> groupedVulnEntries : severityToVulns.entrySet()) {
            String severityValue = groupedVulnEntries.getKey().getValue();
            ComponentItemPriority priority = ComponentItemPriority.findPriority(severityValue);

            List<LinkableItem> vulnAttributes = groupedVulnEntries.getValue()
                                                    .stream()
                                                    .map(vulnItem -> createVulnerabilityAttributeItem(severityValue, vulnItem))
                                                    .collect(Collectors.toList());
            createVulnerabilityPolicyComponentItem(priority, componentItem, componentVersionItem, policyNameItem, policySeverity, notificationId, vulnAttributes)
                .ifPresent(vulnerabilityItems::add);
        }

        return vulnerabilityItems;
    }

    private Optional<ComponentItem> createVulnerabilityPolicyComponentItem(
        ComponentItemPriority priority, LinkableItem component, LinkableItem nullableSubComponent, LinkableItem policy, LinkableItem policySeverity, Long notificationId, Collection<LinkableItem> vulnAttributes) {
        ComponentItem.Builder builder = new ComponentItem.Builder()
                                            .applyCategory(CATEGORY_TYPE)
                                            .applyOperation(ItemOperation.INFO)
                                            .applyPriority(priority)
                                            .applyComponentData(component)
                                            .applySubComponent(nullableSubComponent)
                                            .applyCategoryItem(policy)
                                            .applyCategoryGroupingAttribute(policySeverity)
                                            .applyCollapseOnCategory(false)
                                            .applyAllComponentAttributes(vulnAttributes)
                                            .applyNotificationId(notificationId);
        try {
            return Optional.of(builder.build());
        } catch (AlertException ex) {
            logger.info("Error building policy bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, component, nullableSubComponent);
            logger.error("Error building policy bom edit component cause ", ex);
        }
        return Optional.empty();
    }

    private Map<String, VulnerabilityView> createVulnerabilityViewMap(BlackDuckService blackDuckService, Collection<VulnerableComponentView> vulnerableComponentViews) {
        Set<String> vulnerabilityUrls = new HashSet<>();
        Map<String, VulnerabilityView> vulnerabilityViewMap = new HashMap<>(vulnerableComponentViews.size());
        for (VulnerableComponentView vulnerableComponent : vulnerableComponentViews) {
            Optional<String> vulnerabilitiesLink = vulnerableComponent.getFirstLink(VulnerableComponentView.VULNERABILITIES_LINK);
            if (vulnerabilitiesLink.isPresent() && !vulnerabilityUrls.contains(vulnerabilitiesLink.get())) {
                vulnerabilityViewMap.putAll(vulnerabilityUtil.getVulnerabilitiesForComponent(blackDuckService, vulnerableComponent).stream()
                                                .collect(Collectors.toMap(VulnerabilityView::getName, Function.identity())));
                vulnerabilityUrls.add(vulnerabilitiesLink.get());
            }
        }
        return vulnerabilityViewMap;
    }

    private LinkableItem createVulnerabilityAttributeItem(String severityValue, LinkableItem vulnerabilityItem) {
        String capitalizedSeverityValue = StringUtils.capitalize(severityValue.toLowerCase());
        String attributeName = String.format("%s %s", capitalizedSeverityValue, vulnerabilityItem.getName());

        LinkableItem attributeItem = new LinkableItem(attributeName, vulnerabilityItem.getValue(), vulnerabilityItem.getUrl().orElse(null));
        attributeItem.setCollapsible(vulnerabilityItem.isCollapsible());
        return attributeItem;
    }
}
