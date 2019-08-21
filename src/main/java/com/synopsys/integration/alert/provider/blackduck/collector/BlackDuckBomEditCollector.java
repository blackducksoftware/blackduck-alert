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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckBomEditCollector extends BlackDuckCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckBomEditCollector(JsonExtractor jsonExtractor, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, List.of(BlackDuckContent.BOM_EDIT), blackDuckProperties);
    }

    @Override
    protected List<LinkableItem> getTopicItems(JsonFieldAccessor accessor, List<JsonField<?>> fields) {
        JsonField<String> topicField = getDataField(fields, FieldContentIdentifier.TOPIC);
        return getItemFromProjectVersionWrapper(accessor, topicField, ProjectVersionWrapper::getProjectView,
            view -> new LinkableItem(topicField.getLabel(), ((ProjectView) view).getName(), view.getHref().orElse(null)));
    }

    @Override
    protected List<LinkableItem> getSubTopicItems(JsonFieldAccessor accessor, List<JsonField<?>> fields) {
        JsonField<String> subTopicField = getDataField(fields, FieldContentIdentifier.SUB_TOPIC);
        return getItemFromProjectVersionWrapper(accessor, subTopicField, ProjectVersionWrapper::getProjectVersionView,
            view -> new LinkableItem(subTopicField.getLabel(), ((ProjectVersionView) view).getVersionName(), view.getHref().orElse(null)));
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        JsonField<String> topicField = getDataField(notificationFields, FieldContentIdentifier.CATEGORY_ITEM);
        Optional<String> bomComponentUrl = getBomComponentUrl(jsonFieldAccessor, topicField);
        Optional<VersionBomComponentView> versionBomComponentView = bomComponentUrl.flatMap(url -> getBlackDuckDataHelper().getBomComponentView(url));
        Optional<ProjectVersionWrapper> projectVersionWrapper = versionBomComponentView.flatMap(comp -> getBlackDuckDataHelper().getProjectVersionWrapper(comp));

        List<ComponentItem> componentItems = new LinkedList<>();
        if (versionBomComponentView.isPresent()) {
            // have both the component view and the project wrapper.
            List<LinkableItem> licenseItems = getBlackDuckDataHelper().getLicenseLinkableItems(versionBomComponentView.get());
            componentItems.addAll(addVulnerabilityData(notificationContent.getId(), versionBomComponentView.get(), licenseItems));
            projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(createPolicyItems(notificationContent.getId(), versionWrapper, versionBomComponentView.get(), licenseItems)));
        }

        return componentItems;
    }

    private Collection<ComponentItem> addVulnerabilityData(Long notificationId, VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            LinkableItem componentItem = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent());
            Optional<LinkableItem> componentVersionItem = createComponentVersionItem(versionBomComponent);

            if (doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                List<LinkableItem> componentAttributes = new LinkedList<>();
                componentAttributes.addAll(licenseItems);

                getBucketService().addToTheBucket(getBlackDuckBucket(), versionBomComponent.getComponentVersion(), ComponentVersionView.class);
                ComponentVersionView componentVersionView = getBlackDuckBucket().get(versionBomComponent.getComponentVersion(), ComponentVersionView.class);
                List<LinkableItem> remediationItems = getBlackDuckDataHelper().getRemediationItems(componentVersionView);
                componentAttributes.addAll(remediationItems);

                ComponentItem.Builder builder = new ComponentItem.Builder();
                builder.applyComponentData(componentItem)
                    .applyAllComponentAttributes(componentAttributes)
                    .applyCategory(BlackDuckVulnerabilityCollector.CATEGORY_TYPE)
                    .applyOperation(ItemOperation.UPDATE)
                    .applyNotificationId(notificationId);
                componentVersionItem.ifPresent(builder::applySubComponent);
                try {
                    items.add(builder.build());
                } catch (AlertException ex) {
                    logger
                        .warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentItem, componentVersionItem.orElse(null));
                    logger.error("Error building vulnerability BOM edit component cause ", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("BOM Edit: Error processing vulnerabilities ", ex);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(Long notificationId, ProjectVersionWrapper projectVersionWrapper, VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        if (!PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            LinkableItem componentItem = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent());
            Optional<LinkableItem> componentVersionItem = createComponentVersionItem(versionBomComponent);
            List<VersionBomPolicyRuleView> policyRules = getBlackDuckService().getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (VersionBomPolicyRuleView rule : policyRules) {
                if (!PolicySummaryStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, rule.getName(), null);
                policyNameItem.setCollapsible(true);
                policyNameItem.setSummarizable(true);
                policyNameItem.setCountable(true);
                if (hasVulnerabilityRule(rule)) {
                    List<VulnerableComponentView> vulnerableComponentViews = getBlackDuckService().getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE).stream()
                                                                                 .filter(vulnerableComponentView -> vulnerableComponentView.getComponentName().equals(versionBomComponent.getComponentName()))
                                                                                 .filter(vulnerableComponentView -> vulnerableComponentView.getComponentVersionName().equals(versionBomComponent.getComponentVersionName()))
                                                                                 .collect(Collectors.toList());
                    Map<String, VulnerabilityView> vulnerabilityViews = createVulnerabilityViewMap(vulnerableComponentViews);
                    Set<VulnerabilityWithRemediationView> notificationVulnerabilities = vulnerableComponentViews.stream()
                                                                                            .map(VulnerableComponentView::getVulnerabilityWithRemediation)
                                                                                            .collect(Collectors.toSet());
                    for (VulnerabilityWithRemediationView vulnerabilityView : notificationVulnerabilities) {
                        // TODO to get the URLS for vulnerabilities we would want to traverse the vulnerabilities link
                        String vulnerabilityId = vulnerabilityView.getVulnerabilityName();
                        String vulnerabilityUrl = null;
                        if (vulnerabilityViews.containsKey(vulnerabilityId)) {
                            vulnerabilityUrl = vulnerabilityViews.get(vulnerabilityId).getHref().orElse(null);
                        }
                        String severity = vulnerabilityView.getSeverity().prettyPrint();

                        LinkableItem item = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITIES, vulnerabilityId, vulnerabilityUrl);
                        item.setPartOfKey(true);
                        item.setSummarizable(true);
                        item.setCountable(true);
                        item.setCollapsible(true);

                        LinkableItem severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, severity);
                        severityItem.setSummarizable(true);
                        ComponentItemPriority priority = ComponentItemPriority.findPriority(severity);
                        List<LinkableItem> attributes = new LinkedList<>();
                        attributes.addAll(licenseItems);
                        attributes.add(severityItem);
                        attributes.add(policyNameItem);
                        attributes.add(item);

                        ComponentItem.Builder builder = new ComponentItem.Builder();
                        builder.applyComponentData(componentItem)
                            .applyAllComponentAttributes(attributes)
                            .applyPriority(priority)
                            .applyCategory(BlackDuckPolicyCollector.CATEGORY_TYPE)
                            .applyOperation(ItemOperation.UPDATE)
                            .applyNotificationId(notificationId);
                        componentVersionItem.ifPresent(builder::applySubComponent);
                        try {
                            items.add(builder.build());
                        } catch (AlertException ex) {
                            logger
                                .info("Error building policy bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentItem, componentVersionItem.orElse(null));
                            logger.error("Error building policy bom edit component cause ", ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("BOM Edit: Error processing policy ", ex);
        }

        return items;
    }

    private Map<String, VulnerabilityView> createVulnerabilityViewMap(List<VulnerableComponentView> vulnerableComponentViews) {
        Set<String> vulnerabilityUrls = new HashSet<>();
        Map<String, VulnerabilityView> vulnerabilityViewMap = new HashMap<>(vulnerableComponentViews.size());
        for (VulnerableComponentView vulnerableComponent : vulnerableComponentViews) {
            Optional<String> vulnerabilitiesLink = vulnerableComponent.getFirstLink(VulnerableComponentView.VULNERABILITIES_LINK);
            if (vulnerabilitiesLink.isPresent() && !vulnerabilityUrls.contains(vulnerabilitiesLink.get())) {
                vulnerabilityViewMap.putAll(getVulnerabilitiesForComponent(vulnerableComponent).stream()
                                                .collect(Collectors.toMap(VulnerabilityView::getName, Function.identity())));
                vulnerabilityUrls.add(vulnerabilitiesLink.get());
            }
        }
        return vulnerabilityViewMap;
    }

    private List<VulnerabilityView> getVulnerabilitiesForComponent(VulnerableComponentView vulnerableComponentView) {
        try {
            return getBlackDuckService().getAllResponses(vulnerableComponentView, VulnerableComponentView.VULNERABILITIES_LINK_RESPONSE);
        } catch (IntegrationException ex) {
            logger.error("Error getting vulnerabilities ", ex);
        }
        return List.of();
    }

    private List<LinkableItem> getItemFromProjectVersionWrapper(JsonFieldAccessor accessor, JsonField<String> field, Function<ProjectVersionWrapper, BlackDuckView> viewMapper, Function<BlackDuckView, LinkableItem> itemMapper) {
        List<LinkableItem> items = new ArrayList<>();
        getBomComponentUrl(accessor, field)
            .flatMap(url -> getBlackDuckDataHelper().getBomComponentView(url))
            .flatMap(comp -> getBlackDuckDataHelper().getProjectVersionWrapper(comp))
            .map(viewMapper)
            .map(itemMapper)
            .ifPresent(items::add);

        return items;
    }

    private Optional<LinkableItem> createComponentVersionItem(VersionBomComponentView versionBomComponent) {
        if (StringUtils.isNotBlank(versionBomComponent.getComponentVersionName())) {
            return Optional.of(new LinkableItem(BlackDuckContent.LABEL_COMPONENT_VERSION_NAME, versionBomComponent.getComponentVersionName(), versionBomComponent.getComponentVersion()));
        }
        return Optional.empty();
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

    private Boolean hasVulnerabilityRule(VersionBomPolicyRuleView policyRule) {
        String vulnerabilityCheck = "vuln";
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(vulnerabilityCheck)) {
                return true;
            }
        }
        return false;
    }

    private JsonField<String> getDataField(List<JsonField<?>> fields, FieldContentIdentifier contentIdentifier) {
        Optional<JsonField<?>> optionalField = getFieldForContentIdentifier(fields, contentIdentifier);
        return (JsonField<String>) optionalField.orElseThrow(() -> new AlertRuntimeException(String.format("The '%s' field is required for this operation", contentIdentifier.name())));
    }

    private Optional<String> getBomComponentUrl(JsonFieldAccessor accessor, JsonField<String> field) {
        return accessor.get(field)
                   .stream()
                   .findFirst();
    }

}
