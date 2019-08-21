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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckDataHelper;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityWithRemediationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.log.Slf4jIntLogger;

// Created this class as a parent because of the ObjectFactory bean that is used with Collectors which destroys the bean after use. These services need to be destroyed after usage.
public abstract class BlackDuckCollector extends MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckBucketService bucketService;
    private final BlackDuckService blackDuckService;
    private final BlackDuckBucket blackDuckBucket;
    private final BlackDuckDataHelper blackDuckDataHelper;

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

    protected List<ComponentItem> createVulnerabilityComponentItems(
        Collection<VulnerableComponentView> vulnerableComponentViews, Collection<LinkableItem> licenseItems, LinkableItem policyNameItem, LinkableItem componentItem, Optional<LinkableItem> componentVersionItem, Long notificationId) {
        Map<String, VulnerabilityView> vulnerabilityViews = createVulnerabilityViewMap(vulnerableComponentViews);
        Set<VulnerabilityWithRemediationView> notificationVulnerabilities = vulnerableComponentViews.stream()
                                                                                .map(VulnerableComponentView::getVulnerabilityWithRemediation)
                                                                                .collect(Collectors.toSet());

        List<ComponentItem> vulnerabilityItems = new ArrayList<>();
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
                vulnerabilityItems.add(builder.build());
            } catch (AlertException ex) {
                logger
                    .info("Error building policy bom edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentItem, componentVersionItem.orElse(null));
                logger.error("Error building policy bom edit component cause ", ex);
            }
        }
        return vulnerabilityItems;
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

}
