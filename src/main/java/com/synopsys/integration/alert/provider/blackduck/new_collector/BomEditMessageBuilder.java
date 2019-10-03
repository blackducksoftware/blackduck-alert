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
import java.util.stream.Stream;

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
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityCollector;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.BlackDuckResponseCache;
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.VulnerabilityUtil;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Component
public class BomEditMessageBuilder implements BlackDuckMessageBuilder<BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(BomEditMessageBuilder.class);
    private VulnerabilityUtil vulnerabilityUtil;
    private PolicyPriorityUtil policyPriorityUtil;

    @Autowired
    public BomEditMessageBuilder(final VulnerabilityUtil vulnerabilityUtil, final PolicyPriorityUtil policyPriorityUtil) {
        this.vulnerabilityUtil = vulnerabilityUtil;
        this.policyPriorityUtil = policyPriorityUtil;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.BOM_EDIT.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, BomEditNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BomEditNotificationContent bomEditContent = notificationView.getContent();
        Optional<VersionBomComponentView> bomComponent = responseCache.getBomComponentView(bomEditContent.getBomComponent());
        Optional<ProjectVersionWrapper> projectVersionWrapper = bomComponent.flatMap(responseCache::getProjectVersionWrapper);

        if (bomComponent.isPresent() && projectVersionWrapper.isPresent()) {
            try {
                VersionBomComponentView versionBomComponentView = bomComponent.get();
                ProjectVersionWrapper projectVersionData = projectVersionWrapper.get();
                ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                                  .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                                  .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, projectVersionData.getProjectView().getName())
                                                                                  .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, projectVersionData.getProjectVersionView().getVersionName(),
                                                                                      projectVersionData.getProjectVersionView().getHref().orElse(null))
                                                                                  .applyProviderCreationTime(providerCreationDate);

                List<ComponentItem> componentItems = new LinkedList<>();
                List<LinkableItem> commonAttributes = Stream.concat(getLicenseLinkableItems(bomComponent.get()).stream(), getUsageLinkableItems(bomComponent.get()).stream()).collect(Collectors.toList());

                componentItems.addAll(addVulnerabilityData(responseCache, componentService, notificationId, versionBomComponentView, projectVersionData, commonAttributes));
                projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(createPolicyItems(responseCache, blackDuckService, notificationId, versionWrapper, versionBomComponentView, commonAttributes)));

                projectVersionMessageBuilder.applyAllComponentItems(componentItems);
                return List.of(projectVersionMessageBuilder.build());
            } catch (AlertException ex) {
                logger.error("Error creating policy violation message.", ex);
            }
        }
        return List.of();
    }

    // TODO each MessageBuilder has a copy of these methods create a utility to encapsulate this code
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

    private Collection<ComponentItem> addVulnerabilityData(BlackDuckResponseCache blackDuckResponseCache, ComponentService componentService, Long notificationId, VersionBomComponentView versionBomComponent,
        ProjectVersionWrapper projectVersionWrapper, List<LinkableItem> commonAttributes) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectQueryLink = blackDuckResponseCache.getProjectComponentQueryLink(projectVersionWrapper.getProjectVersionView().getHref().orElse(null), ProjectVersionView.VULNERABLE_COMPONENTS_LINK, componentName).orElse(null);
            LinkableItem componentItem;
            LinkableItem componentVersionItem = null;
            if (StringUtils.isNotBlank(componentVersionName)) {
                componentVersionItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_VERSION_NAME, componentVersionName, projectQueryLink);
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName);
            } else {
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName, projectQueryLink);
            }

            if (doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                List<LinkableItem> componentAttributes = new LinkedList<>();
                componentAttributes.addAll(commonAttributes);

                Optional<ComponentVersionView> componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, versionBomComponent.getComponentVersion());
                if (componentVersionView.isPresent()) {
                    List<LinkableItem> remediationItems = vulnerabilityUtil.getRemediationItems(componentService, componentVersionView.get());
                    componentAttributes.addAll(remediationItems);
                }
                ComponentItem.Builder builder = new ComponentItem.Builder()
                                                    .applyCategory(BlackDuckVulnerabilityCollector.CATEGORY_TYPE)
                                                    .applyOperation(ItemOperation.UPDATE)
                                                    .applyComponentData(componentItem)
                                                    .applySubComponent(componentVersionItem)
                                                    // FIXME get the vulnerability id(s) and create a ComponentItem from each of them
                                                    .applyCategoryItem(new LinkableItem(BlackDuckContent.LABEL_VULNERABILITIES, "Present"))
                                                    .applyCollapseOnCategory(true)
                                                    .applyAllComponentAttributes(componentAttributes)
                                                    .applyNotificationId(notificationId);
                try {
                    items.add(builder.build());
                } catch (AlertException alertException) {
                    logger
                        .warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentItem, componentVersionItem);
                    logger.error("Error building vulnerability BOM edit component cause ", alertException);
                }
            }
        } catch (Exception genericException) {
            logger.error("BOM Edit: Error processing vulnerabilities ", genericException);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, Long notificationId, ProjectVersionWrapper projectVersionWrapper,
        VersionBomComponentView versionBomComponent, List<LinkableItem> commonAttributes) {
        if (!PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectQueryLink = blackDuckResponseCache.getProjectComponentQueryLink(projectVersionWrapper.getProjectVersionView().getHref().orElse(null), ProjectVersionView.VULNERABLE_COMPONENTS_LINK, componentName).orElse(null);
            LinkableItem componentItem;
            LinkableItem componentVersionItem = null;
            if (StringUtils.isNotBlank(componentVersionName)) {
                componentVersionItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_VERSION_NAME, componentVersionName, projectQueryLink);
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName);
            } else {
                componentItem = new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentName, projectQueryLink);
            }
            List<VersionBomPolicyRuleView> policyRules = blackDuckService.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (VersionBomPolicyRuleView rule : policyRules) {
                if (!PolicySummaryStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, rule.getName(), null);
                LinkableItem policySeverityItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_SEVERITY_NAME, rule.getSeverity());
                if (hasVulnerabilityRule(rule)) {
                    List<VulnerableComponentView> vulnerableComponentViews = vulnerabilityUtil.getVulnerableComponentViews(blackDuckService, projectVersionWrapper, versionBomComponent);
                    List<ComponentItem> vulnerabilityComponentItems =
                        createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverityItem, componentItem, componentVersionItem, notificationId, blackDuckService, blackDuckResponseCache);
                    items.addAll(vulnerabilityComponentItems);
                } else {
                    items.add(createPolicyComponentItem(notificationId, rule, componentItem, componentVersionItem, policyNameItem, commonAttributes));
                }
            }
        } catch (Exception e) {
            logger.error("BOM Edit: Error processing policy ", e);
        }

        return items;
    }

    public boolean hasVulnerabilityRule(VersionBomPolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(MessageBuilderConstants.VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesSecurityRiskProfileHaveVulnerabilities(final RiskProfileView securityRiskProfile) {
        logger.debug("Checking if the component still has vulnerabilities...");
        int vulnerabilitiesCount = getSumOfRiskCounts(securityRiskProfile.getCounts());
        logger.debug("Number of vulnerabilities found: " + vulnerabilitiesCount);
        if (vulnerabilitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        }
        return false;
    }

    private int getSumOfRiskCounts(List<RiskCountView> vulnerabilityCounts) {
        int count = 0;
        for (RiskCountView riskCount : vulnerabilityCounts) {
            if (!RiskCountType.OK.equals(riskCount.getCountType())) {
                count += riskCount.getCount();
            }
        }
        return count;
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
                                            .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
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

    private ComponentItem createPolicyComponentItem(Long notificationId, VersionBomPolicyRuleView rule, LinkableItem componentItem, LinkableItem componentVersionItem, LinkableItem policyNameItem, List<LinkableItem> attributes)
        throws AlertException {
        return new ComponentItem.Builder()
                   .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                   .applyOperation(ItemOperation.UPDATE)
                   .applyPriority(policyPriorityUtil.getPriorityFromSeverity(rule.getSeverity()))
                   .applyComponentData(componentItem)
                   .applySubComponent(componentVersionItem)
                   .applyCategoryItem(policyNameItem)
                   .applyAllComponentAttributes(attributes)
                   .applyNotificationId(notificationId)
                   .build();
    }

}
