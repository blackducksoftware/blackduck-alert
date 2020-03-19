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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckDataHelper;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckBomEditCollector extends BlackDuckCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckBomEditCollector(JsonExtractor jsonExtractor, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, List.of(BlackDuckContent.BOM_EDIT), blackDuckProperties);
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        JsonField<String> topicField = getDataField(notificationFields, FieldContentIdentifier.CATEGORY_ITEM);
        Optional<String> bomComponentUrl = getBomComponentUrl(jsonFieldAccessor, topicField);

        BlackDuckDataHelper blackDuckDataHelper = getBlackDuckDataHelper();
        Optional<VersionBomComponentView> versionBomComponentView = bomComponentUrl.flatMap(blackDuckDataHelper::getBomComponentView);
        Optional<ProjectVersionWrapper> projectVersionWrapper = versionBomComponentView.flatMap(blackDuckDataHelper::getProjectVersionWrapper);

        List<ComponentItem> componentItems = new LinkedList<>();
        if (versionBomComponentView.isPresent()) {
            // have both the component view and the project wrapper.
            List<LinkableItem> licenseItems = blackDuckDataHelper.getLicenseLinkableItems(versionBomComponentView.get());
            componentItems.addAll(addVulnerabilityData(notificationContent.getId(), versionBomComponentView.get(), licenseItems));
            projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(createPolicyItems(notificationContent.getId(), versionWrapper, versionBomComponentView.get(), licenseItems)));
        }

        return componentItems;
    }

    private Collection<ComponentItem> addVulnerabilityData(Long notificationId, VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            Optional<LinkableItem> componentVersionItem = createComponentVersionItem(versionBomComponent);
            // for 5.0.0 make the BOM edit either include the component link or the component version link not both to be consistent with the other channels.
            LinkableItem componentItem = componentVersionItem
                                             .map(ignored -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName()))
                                             .orElse(new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent()));
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
                } catch (AlertException alertException) {
                    logger
                        .warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentItem, componentVersionItem.orElse(null));
                    logger.error("Error building vulnerability BOM edit component cause ", alertException);
                }
            }
        } catch (Exception genericException) {
            logger.error("BOM Edit: Error processing vulnerabilities ", genericException);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(Long notificationId, ProjectVersionWrapper projectVersionWrapper, VersionBomComponentView versionBomComponent, List<LinkableItem> licenseItems) {
        if (!PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            Optional<LinkableItem> componentVersionItem = createComponentVersionItem(versionBomComponent);
            // for 5.0.0 make the BOM edit either include the component link or the component version link not both to be consistent with the other channels.
            LinkableItem componentItem = componentVersionItem
                                             .map(ignored -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName()))
                                             .orElse(new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, versionBomComponent.getComponentName(), versionBomComponent.getComponent()));
            List<VersionBomPolicyRuleView> policyRules = getBlackDuckService().getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (VersionBomPolicyRuleView rule : policyRules) {
                if (!PolicySummaryStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, rule.getName(), null);
                policyNameItem.setCollapsible(true);
                policyNameItem.setSummarizable(true);
                policyNameItem.setCountable(true);
                if (getBlackDuckDataHelper().hasVulnerabilityRule(rule)) {
                    List<VulnerableComponentView> vulnerableComponentViews = getBlackDuckDataHelper().getVulnerableComponentViews(projectVersionWrapper, versionBomComponent);
                    List<ComponentItem> vulnerabilityComponentItems = createVulnerabilityComponentItems(vulnerableComponentViews, licenseItems, policyNameItem, componentItem, componentVersionItem, notificationId, ItemOperation.UPDATE);
                    items.addAll(vulnerabilityComponentItems);
                } else {
                    items.add(createPolicyComponentItem(notificationId, rule, componentItem, componentVersionItem.orElse(null), policyNameItem, licenseItems));
                }
            }
        } catch (Exception e) {
            logger.error("BOM Edit: Error processing policy ", e);
        }

        return items;
    }

    private List<LinkableItem> getItemFromProjectVersionWrapper(JsonFieldAccessor accessor, JsonField<String> field, Function<ProjectVersionWrapper, BlackDuckView> viewMapper, Function<BlackDuckView, LinkableItem> itemMapper) {
        List<LinkableItem> items = new ArrayList<>();
        BlackDuckDataHelper blackDuckDataHelper = getBlackDuckDataHelper();
        getBomComponentUrl(accessor, field)
            .flatMap(blackDuckDataHelper::getBomComponentView)
            .flatMap(blackDuckDataHelper::getProjectVersionWrapper)
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

    private JsonField<String> getDataField(List<JsonField<?>> fields, FieldContentIdentifier contentIdentifier) {
        Optional<JsonField<?>> optionalField = getFieldForContentIdentifier(fields, contentIdentifier);
        return (JsonField<String>) optionalField.orElseThrow(() -> new AlertRuntimeException(String.format("The '%s' field is required for this operation", contentIdentifier.name())));
    }

    private Optional<String> getBomComponentUrl(JsonFieldAccessor accessor, JsonField<String> field) {
        return accessor.get(field)
                   .stream()
                   .findFirst();
    }

    private ComponentItem createPolicyComponentItem(Long notificationId, VersionBomPolicyRuleView rule, LinkableItem componentItem, LinkableItem componentVersionItem, LinkableItem policyNameItem, List<LinkableItem> licenseItems)
        throws AlertException {
        ComponentItem.Builder builder = new ComponentItem.Builder();

        builder.applyComponentData(componentItem)
            .applyComponentAttribute(policyNameItem)
            .applyAllComponentAttributes(licenseItems)
            .applySubComponent(componentVersionItem)
            .applyPriority(getPolicyPriority(rule.getSeverity()))
            .applyCategory(BlackDuckPolicyCollector.CATEGORY_TYPE)
            .applyOperation(ItemOperation.UPDATE)
            .applyNotificationId(notificationId);
        return builder.build();
    }

}
