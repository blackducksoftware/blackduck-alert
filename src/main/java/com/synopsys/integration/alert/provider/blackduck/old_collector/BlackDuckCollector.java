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
package com.synopsys.integration.alert.provider.blackduck.old_collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.old_collector.util.BlackDuckDataHelper;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

// Created this class as a parent because of the ObjectFactory bean that is used with Collectors which destroys the bean after use. These services need to be destroyed after usage.
public abstract class BlackDuckCollector extends MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckBucketService bucketService;
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;
    private final BlackDuckDataHelper blackDuckDataHelper;
    private final Map<String, ComponentItemPriority> policyPriorityMap = new HashMap<>();

    public BlackDuckCollector(JsonExtractor jsonExtractor, Collection<ProviderContentType> contentTypes, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, contentTypes);
        this.blackDuckProperties = blackDuckProperties;

        Optional<BlackDuckServicesFactory> blackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                                                                          .map(blackDuckHttpClient -> blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger)));
        blackDuckService = blackDuckServicesFactory
                               .map(BlackDuckServicesFactory::createBlackDuckService)
                               .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        bucketService = blackDuckServicesFactory
                            .map(BlackDuckServicesFactory::createBlackDuckBucketService)
                            .orElseThrow(() -> new AlertRuntimeException("The BlackDuckCollector cannot be used without a valid Black Duck connection"));
        blackDuckBucket = new BlackDuckBucket();
        blackDuckDataHelper = new BlackDuckDataHelper(blackDuckProperties, blackDuckService, blackDuckBucket, bucketService);

        policyPriorityMap.put("blocker", ComponentItemPriority.HIGHEST);
        policyPriorityMap.put("critical", ComponentItemPriority.HIGH);
        policyPriorityMap.put("major", ComponentItemPriority.MEDIUM);
        policyPriorityMap.put("minor", ComponentItemPriority.LOW);
        policyPriorityMap.put("trivial", ComponentItemPriority.LOWEST);
        policyPriorityMap.put("unspecified", ComponentItemPriority.NONE);
    }

    public BlackDuckService getBlackDuckService() {
        return blackDuckService;
    }

    protected BlackDuckBucketService getBucketService() {
        return bucketService;
    }

    protected BlackDuckBucket getBlackDuckBucket() {
        return blackDuckBucket;
    }

    public BlackDuckDataHelper getBlackDuckDataHelper() {
        return blackDuckDataHelper;
    }

    @Override
    protected LinkableItem getProviderItem() {
        final String blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(null);
        return new LinkableItem(ProviderMessageContent.LABEL_PROVIDER, "Black Duck", blackDuckUrl);
    }

    protected List<ComponentItem> createVulnerabilityPolicyComponentItems(Collection<VulnerableComponentView> vulnerableComponentViews, LinkableItem policyNameItem, LinkableItem policySeverity,
        LinkableItem componentItem, Optional<LinkableItem> componentVersionItem, Long notificationId) {
        Map<String, VulnerabilityView> vulnerabilityViews = createVulnerabilityViewMap(vulnerableComponentViews);
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

            LinkableItem vulnerabilityIdItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITIES, vulnerabilityId, vulnerabilityUrl);
            vulnerabilityIdItem.setCollapsible(true);
            LinkableItem severityItem = getSeverity(vulnerabilityUrl);

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
            createVulnerabilityPolicyComponentItem(priority, componentItem, componentVersionItem.orElse(null), policyNameItem, policySeverity, notificationId, vulnAttributes)
                .ifPresent(vulnerabilityItems::add);
        }

        return vulnerabilityItems;
    }

    protected Optional<ComponentItem> createRemediationComponentItem(String categoryType, ComponentVersionView componentVersionView, LinkableItem componentItem, Optional<LinkableItem> componentVersionItem,
        LinkableItem categoryItem, LinkableItem categoryGrouping, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = getBlackDuckDataHelper().getRemediationItems(componentVersionView);
            if (!remediationItems.isEmpty()) {
                ComponentItem.Builder remediationComponent = new ComponentItem.Builder()
                                                                 .applyCategory(categoryType)
                                                                 .applyOperation(ItemOperation.INFO)
                                                                 .applyPriority(ComponentItemPriority.NONE)
                                                                 .applyComponentData(componentItem)
                                                                 .applyCategoryItem(categoryItem)
                                                                 .applyCategoryGroupingAttribute(categoryGrouping)
                                                                 .applyCollapseOnCategory(collapseOnCategory)
                                                                 .applyAllComponentAttributes(remediationItems)
                                                                 .applyNotificationId(notificationId);
                componentVersionItem.ifPresent(remediationComponent::applySubComponent);

                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

    protected LinkableItem getSeverity(String vulnerabilityUrl) {
        LinkableItem severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, "UNKNOWN");
        try {
            getBucketService().addToTheBucket(getBlackDuckBucket(), vulnerabilityUrl, VulnerabilityView.class);
            VulnerabilityView vulnerabilityView = getBlackDuckBucket().get(vulnerabilityUrl, VulnerabilityView.class);
            String severity = vulnerabilityView.getSeverity();
            Optional<String> cvss3Severity = getCvss3Severity(vulnerabilityView);
            if (cvss3Severity.isPresent()) {
                severity = cvss3Severity.get();
            }
            severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, severity);
        } catch (Exception e) {
            logger.debug("Error fetching vulnerability view", e);
        }

        return severityItem;
    }

    protected ComponentItemPriority getPolicyPriority(String severity) {
        if (StringUtils.isNotBlank(severity)) {
            String severityKey = severity.trim().toLowerCase();
            return policyPriorityMap.getOrDefault(severityKey, ComponentItemPriority.NONE);
        }
        return ComponentItemPriority.NONE;
    }

    // TODO update this code with an Object from blackduck-common-api when available
    private Optional<String> getCvss3Severity(VulnerabilityView vulnerabilityView) {
        Boolean useCvss3 = vulnerabilityView.getUseCvss3();
        if (null != useCvss3 && useCvss3) {
            JsonObject vulnJsonObject = vulnerabilityView.getJsonElement().getAsJsonObject();
            JsonElement cvss3 = vulnJsonObject.get("cvss3");
            if (null != cvss3) {
                JsonElement cvss3Severity = cvss3.getAsJsonObject().get("severity");
                if (null != cvss3Severity) {
                    return Optional.of(cvss3Severity.getAsString());
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, VulnerabilityView> createVulnerabilityViewMap(Collection<VulnerableComponentView> vulnerableComponentViews) {
        Set<String> vulnerabilityUrls = new HashSet<>();
        Map<String, VulnerabilityView> vulnerabilityViewMap = new HashMap<>(vulnerableComponentViews.size());
        for (VulnerableComponentView vulnerableComponent : vulnerableComponentViews) {
            Optional<String> vulnerabilitiesLink = vulnerableComponent.getFirstLink(VulnerableComponentView.VULNERABILITIES_LINK);
            if (vulnerabilitiesLink.isPresent() && !vulnerabilityUrls.contains(vulnerabilitiesLink.get())) {
                vulnerabilityViewMap.putAll(getBlackDuckDataHelper().getVulnerabilitiesForComponent(vulnerableComponent).stream()
                                                .collect(Collectors.toMap(VulnerabilityView::getName, Function.identity())));
                vulnerabilityUrls.add(vulnerabilitiesLink.get());
            }
        }
        return vulnerabilityViewMap;
    }

    private Optional<ComponentItem> createVulnerabilityPolicyComponentItem(
        ComponentItemPriority priority, LinkableItem component, LinkableItem nullableSubComponent, LinkableItem policy, LinkableItem policySeverity, Long notificationId, Collection<LinkableItem> vulnAttributes) {
        ComponentItem.Builder builder = new ComponentItem.Builder()
                                            .applyCategory(BlackDuckPolicyCollector.CATEGORY_TYPE)
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

    private LinkableItem createVulnerabilityAttributeItem(String severityValue, LinkableItem vulnerabilityItem) {
        String capitalizedSeverityValue = StringUtils.capitalize(severityValue.toLowerCase());
        String attributeName = String.format("%s %s", capitalizedSeverityValue, vulnerabilityItem.getName());

        LinkableItem attributeItem = new LinkableItem(attributeName, vulnerabilityItem.getValue(), vulnerabilityItem.getUrl().orElse(null));
        attributeItem.setCollapsible(vulnerabilityItem.isCollapsible());
        return attributeItem;
    }

}
