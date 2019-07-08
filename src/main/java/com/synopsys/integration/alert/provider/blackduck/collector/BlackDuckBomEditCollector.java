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
import com.synopsys.integration.blackduck.api.generated.component.RemediatingVersionView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckBomEditCollector extends BlackDuckCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckBomEditCollector(final JsonExtractor jsonExtractor, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, List.of(BlackDuckContent.BOM_EDIT), blackDuckProperties);
    }

    @Override
    protected List<LinkableItem> getTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        final JsonField<String> topicField = getDataField(fields, FieldContentIdentifier.TOPIC);
        return getItemFromProjectVersionWrapper(accessor, topicField, ProjectVersionWrapper::getProjectView,
            view -> new LinkableItem(topicField.getLabel(), ((ProjectView) view).getName(), view.getHref().orElse(null)));
    }

    @Override
    protected List<LinkableItem> getSubTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        final JsonField<String> subTopicField = getDataField(fields, FieldContentIdentifier.SUB_TOPIC);
        return getItemFromProjectVersionWrapper(accessor, subTopicField, ProjectVersionWrapper::getProjectVersionView,
            view -> new LinkableItem(subTopicField.getLabel(), ((ProjectVersionView) view).getVersionName(), view.getHref().orElse(null)));
    }

    private List<LinkableItem> getItemFromProjectVersionWrapper(JsonFieldAccessor accessor, JsonField<String> field, Function<ProjectVersionWrapper, BlackDuckView> viewMapper, Function<BlackDuckView, LinkableItem> itemMapper) {
        final List<LinkableItem> items = new ArrayList<>();

        getBomComponentUrl(accessor, field)
            .flatMap(this::getBomComponentView)
            .flatMap(this::getProjectVersionWrapper)
            .map(viewMapper)
            .map(itemMapper)
            .ifPresent(items::add);

        return items;
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final JsonField<String> topicField = getDataField(notificationFields, FieldContentIdentifier.CATEGORY_ITEM);
        final Optional<String> bomComponentUrl = getBomComponentUrl(jsonFieldAccessor, topicField);
        final Optional<VersionBomComponentView> versionBomComponentView = bomComponentUrl.flatMap(this::getBomComponentView);
        final Optional<ProjectVersionWrapper> projectVersionWrapper = versionBomComponentView.flatMap(this::getProjectVersionWrapper);

        List<ComponentItem> componentItems = new LinkedList<>();
        if (versionBomComponentView.isPresent()) {
            // have both the component view and the project wrapper.
            List<LinkableItem> licenseItems = getLicenseLinkableItems(versionBomComponentView.get());
            componentItems.addAll(addVulnerabilityData(notificationContent.getId(), versionBomComponentView.get(), licenseItems));
            projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(addPolicyData(notificationContent.getId(), versionWrapper, versionBomComponentView.get(), licenseItems)));
        }

        return componentItems;
    }

    private JsonField<String> getDataField(final List<JsonField<?>> fields, final FieldContentIdentifier contentIdentifier) {
        final Optional<JsonField<?>> optionalField = getFieldForContentIdentifier(fields, contentIdentifier);
        return (JsonField<String>) optionalField.orElseThrow(() -> new AlertRuntimeException(String.format("The '%s' field is required for this operation", contentIdentifier.name())));
    }

    private Optional<String> getBomComponentUrl(final JsonFieldAccessor accessor, JsonField<String> field) {
        return accessor.get(field)
                   .stream()
                   .findFirst();
    }

    //TODO Clean up this class to make the code more elegant.  This code was based on the Jira Plugin.  Currently it functions but needs more work to make it production ready.
    // TODO use the bucket service especially for the vulnerabilities

    private Optional<ProjectVersionWrapper> getProjectVersionWrapper(final VersionBomComponentView versionBomComponent) {
        try {
            // TODO Stop using this when Black Duck supports going back to the project-version
            final Optional<String> versionBomComponentHref = versionBomComponent.getHref();
            if (versionBomComponentHref.isPresent()) {
                final String versionHref = versionBomComponentHref.get();
                final int componentsIndex = versionHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
                final String projectVersionUri = versionHref.substring(0, componentsIndex - 1);

                getBucketService().addToTheBucket(getBlackDuckBucket(), projectVersionUri, ProjectVersionView.class);
                final ProjectVersionView projectVersion = getBlackDuckBucket().get(projectVersionUri, ProjectVersionView.class);
                final ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
                wrapper.setProjectVersionView(projectVersion);
                getBlackDuckService().getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE).ifPresent(wrapper::setProjectView);
                return Optional.of(wrapper);
            }
        } catch (final IntegrationException ie) {
            logger.error("Error getting project version for Bom Component. ", ie);
        }

        return Optional.empty();
    }

    private Collection<ComponentItem> addVulnerabilityData(Long notificationId, VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            final RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            final LinkableItem componentItem = getComponentLinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent());
            Optional<LinkableItem> componentVersionItem = getComponentVersionLinkableItem(versionBomComponent);

            if (doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                getBucketService().addToTheBucket(getBlackDuckBucket(), versionBomComponent.getComponentVersion(), ComponentVersionView.class);
                ComponentVersionView componentVersionView = getBlackDuckBucket().get(versionBomComponent.getComponentVersion(), ComponentVersionView.class);
                // add remediation data information.
                final ComponentService componentService = new ComponentService(getBlackDuckService(), new Slf4jIntLogger(logger));
                final Optional<RemediationOptionsView> optionalRemediation = componentService.getRemediationInformation(componentVersionView);

                if (optionalRemediation.isPresent()) {
                    final List<LinkableItem> componentAttributes = new LinkedList<>();
                    componentAttributes.addAll(licenseItems);
                    final RemediationOptionsView remediationOptions = optionalRemediation.get();
                    componentAttributes.addAll(getRemediationAttributes(remediationOptions));

                    ComponentItem.Builder builder = new ComponentItem.Builder();
                    builder.applyComponentData(componentItem)
                        .applyAllComponentAttributes(componentAttributes)
                        .applyCategory(BlackDuckVulnerabilityCollector.CATEGORY_TYPE)
                        .applyOperation(ItemOperation.ADD)
                        .applyNotificationId(notificationId);
                    componentVersionItem.ifPresent(builder::applySubComponent);
                    try {
                        items.add(builder.build());
                    } catch (AlertException ex) {
                        logger
                            .info("Error building vulnerability bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.ADD, componentItem, componentVersionItem.orElse(null));
                        logger.error("Error building vulnerability bom edit component cause ", ex);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("BOM Edit: Error processing vulnerabilities ", ex);
        }
        return items;
    }

    private Collection<LinkableItem> getRemediationAttributes(RemediationOptionsView remediationOptions) {
        final List<LinkableItem> attributes = new LinkedList<>();
        if (null != remediationOptions.getFixesPreviousVulnerabilities()) {
            RemediatingVersionView remediatingVersionView = remediationOptions.getFixesPreviousVulnerabilities();
            String versionText = createRemediationVersionText(remediatingVersionView);
            attributes.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_FIX_PREVIOUS, versionText, remediatingVersionView.getComponentVersion()));
        }
        if (null != remediationOptions.getLatestAfterCurrent()) {
            RemediatingVersionView remediatingVersionView = remediationOptions.getLatestAfterCurrent();
            String versionText = createRemediationVersionText(remediatingVersionView);
            attributes.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_LATEST, versionText, remediatingVersionView.getComponentVersion()));
        }
        if (null != remediationOptions.getNoVulnerabilities()) {
            RemediatingVersionView remediatingVersionView = remediationOptions.getNoVulnerabilities();
            String versionText = createRemediationVersionText(remediatingVersionView);
            attributes.add(new LinkableItem(BlackDuckContent.LABEL_REMEDIATION_CLEAN, versionText, remediatingVersionView.getComponentVersion()));
        }

        return attributes;
    }

    private Collection<ComponentItem> addPolicyData(Long notificationId, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            final LinkableItem componentItem = getComponentLinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent());
            Optional<LinkableItem> componentVersionItem = getComponentVersionLinkableItem(versionBomComponent);
            final List<PolicyRuleView> policyRules = getBlackDuckService().getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (final PolicyRuleView rule : policyRules) {
                final LinkableItem policyNameItem = getComponentLinkableItem(BlackDuckContent.LABEL_POLICY_NAME, rule.getName(), null);
                policyNameItem.setCollapsible(true);
                policyNameItem.setSummarizable(true);
                policyNameItem.setCountable(true);
                if (hasVulnerabilityRule(rule)) {
                    final List<VulnerableComponentView> vulnerableComponentViews = getBlackDuckService().getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE).stream()
                                                                                       .filter(vulnerableComponentView -> vulnerableComponentView.getComponentName().equals(versionBomComponent.getComponentName()))
                                                                                       .filter(vulnerableComponentView -> vulnerableComponentView.getComponentVersionName().equals(versionBomComponent.getComponentVersionName()))
                                                                                       .collect(Collectors.toList());

                    final Map<String, VulnerabilityView> vulnerabilityViews = createVulnerabilityViewMap(vulnerableComponentViews);

                    final Set<VulnerabilityWithRemediationView> notificationVulnerabilities = vulnerableComponentViews.stream()
                                                                                                  .map(VulnerableComponentView::getVulnerabilityWithRemediation)
                                                                                                  .collect(Collectors.toSet());

                    for (VulnerabilityWithRemediationView vulnerabilityView : notificationVulnerabilities) {
                        String vulnerabilityName = vulnerabilityView.getVulnerabilityName();
                        String vulnerabilitySeverity = vulnerabilityView.getSeverity().prettyPrint();
                        String vulnerabilityUrl = null;
                        if (vulnerabilityViews.containsKey(vulnerabilityName)) {
                            vulnerabilityUrl = vulnerabilityViews.get(vulnerabilityName).getHref().orElse(null);
                        }
                        ComponentItem.Builder builder = createPolicyItemBuilder(notificationId, licenseItems, componentItem, componentVersionItem, policyNameItem, vulnerabilityName, vulnerabilityUrl, vulnerabilitySeverity);
                        try {
                            items.add(builder.build());
                        } catch (AlertException ex) {
                            logger.info("Error building policy bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.ADD, componentItem, componentVersionItem.orElse(null));
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

    private ComponentItem.Builder createPolicyItemBuilder(Long notificationId, List<LinkableItem> licenseItems, LinkableItem componentItem, Optional<LinkableItem> componentVersionItem, LinkableItem policyNameItem, String vulnerabilityId,
        String vulnerabilityUrl, String severity) {
        final LinkableItem item = getComponentLinkableItem(BlackDuckContent.LABEL_VULNERABILITIES, vulnerabilityId, vulnerabilityUrl);
        item.setPartOfKey(true);
        item.setSummarizable(true);
        item.setCountable(true);
        item.setCollapsible(true);

        final LinkableItem severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, severity);
        severityItem.setSummarizable(true);
        final ComponentItemPriority priority = ComponentItemPriority.findPriority(severity);
        final List<LinkableItem> attributes = new LinkedList<>();
        attributes.addAll(licenseItems);
        attributes.add(severityItem);
        attributes.add(policyNameItem);
        attributes.add(item);

        ComponentItem.Builder builder = new ComponentItem.Builder();
        builder.applyComponentData(componentItem)
            .applyAllComponentAttributes(attributes)
            .applyPriority(priority)
            .applyCategory(BlackDuckPolicyCollector.CATEGORY_TYPE)
            .applyOperation(ItemOperation.ADD)
            .applyNotificationId(notificationId);
        componentVersionItem.ifPresent(builder::applySubComponent);
        return builder;
    }

    private List<VulnerabilityView> getVulnerabilitiesForComponent(VulnerableComponentView vulnerableComponentView) {
        try {
            return getBlackDuckService().getAllResponses(vulnerableComponentView, VulnerableComponentView.VULNERABILITIES_LINK_RESPONSE);
        } catch (IntegrationException ex) {
            logger.error("Error getting vulnerabilities ", ex);
        }
        return List.of();
    }

    private LinkableItem getComponentLinkableItem(final String labelComponentName, final String componentName, final String component) {
        return new LinkableItem(labelComponentName, componentName, component);
    }

    private Optional<LinkableItem> getComponentVersionLinkableItem(final VersionBomComponentView versionBomComponent) {
        Optional<LinkableItem> componentVersionItem = Optional.empty();
        if (StringUtils.isNotBlank(versionBomComponent.getComponentVersionName())) {
            componentVersionItem = Optional.of(new LinkableItem(BlackDuckContent.LABEL_COMPONENT_VERSION_NAME, versionBomComponent.getComponentVersionName(), versionBomComponent.getComponentVersion()));
        }
        return componentVersionItem;
    }

    private boolean doesSecurityRiskProfileHaveVulnerabilities(final RiskProfileView securityRiskProfile) {
        logger.debug("Checking if the component still has vulnerabilities...");
        final int vulnerablitiesCount = getSumOfRiskCounts(securityRiskProfile.getCounts());
        logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
        if (vulnerablitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        }
        return false;
    }

    private int getSumOfRiskCounts(final List<RiskCountView> vulnerabilityCounts) {
        int count = 0;
        for (final RiskCountView riskCount : vulnerabilityCounts) {
            if (!RiskCountType.OK.equals(riskCount.getCountType())) {
                count += riskCount.getCount();
            }
        }
        return count;
    }

    private String createRemediationVersionText(final RemediatingVersionView remediatingVersionView) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(remediatingVersionView.getName());
        if (remediatingVersionView.getVulnerabilityCount() != null && remediatingVersionView.getVulnerabilityCount() > 0) {
            stringBuilder.append(" (Vulnerability Count: ");
            stringBuilder.append(remediatingVersionView.getVulnerabilityCount());
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    private Boolean hasVulnerabilityRule(final PolicyRuleView policyRuleView) {
        final String vulnerabilityCheck = "vuln";
        final PolicyRuleExpressionSetView expression = policyRuleView.getExpression();
        final List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (final PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(vulnerabilityCheck)) {
                return true;
            }
        }
        return false;
    }

}
