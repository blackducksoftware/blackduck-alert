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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public abstract class BlackDuckPolicyCollector extends BlackDuckCollector {
    public static final String CATEGORY_TYPE = "Policy";
    private final Map<String, ComponentItemPriority> priorityMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlackDuckPolicyCollector(JsonExtractor jsonExtractor, Collection<ProviderContentType> contentTypes, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, contentTypes, blackDuckProperties);

        priorityMap.put("blocker", ComponentItemPriority.HIGHEST);
        priorityMap.put("critical", ComponentItemPriority.HIGH);
        priorityMap.put("major", ComponentItemPriority.MEDIUM);
        priorityMap.put("minor", ComponentItemPriority.LOW);
        priorityMap.put("trivial", ComponentItemPriority.LOWEST);
        priorityMap.put("unspecified", ComponentItemPriority.NONE);
    }

    protected Optional<ComponentItem> addApplicableItems(ItemOperation operation, ComponentItemPriority priority, LinkableItem componentItem, LinkableItem componentVersionItem, LinkableItem policyItem, LinkableItem policySeverityItem,
        Collection<LinkableItem> policyAttributes, Long notificationId) {
        try {
            ComponentItem.Builder builder = new ComponentItem.Builder()
                                                .applyCategory(CATEGORY_TYPE)
                                                .applyOperation(operation)
                                                .applyPriority(priority)
                                                .applyComponentData(componentItem)
                                                .applySubComponent(componentVersionItem)
                                                .applyCategoryItem(policyItem)
                                                .applyCategoryGroupingAttribute(policySeverityItem)
                                                .applyAllComponentAttributes(policyAttributes)
                                                .applyNotificationId(notificationId);
            return Optional.of(builder.build());
        } catch (Exception ex) {
            logger.info("Error building policy component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentItem, componentVersionItem);
            logger.error("Error building policy component cause ", ex);
            return Optional.empty();
        }
    }

    protected LinkableItem createPolicyNameItem(PolicyInfo policyInfo) {
        String policyName = policyInfo.getPolicyName();
        return new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, policyName, null);
    }

    protected Optional<LinkableItem> createPolicySeverityItem(PolicyInfo policyInfo) {
        String severity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(severity)) {
            final LinkableItem severityItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_SEVERITY_NAME, severity, null);
            return Optional.of(severityItem);
        }
        return Optional.empty();
    }

    protected List<LinkableItem> createLicenseLinkableItems(String bomComponentUrl) {
        return createLinkableItems(bomComponentUrl, getBlackDuckDataHelper()::getLicenseLinkableItems);
    }

    protected List<LinkableItem> createUsageLinkableItems(String bomComponentUrl) {
        return createLinkableItems(bomComponentUrl, getBlackDuckDataHelper()::getUsageLinkableItems);
    }

    private List<LinkableItem> createLinkableItems(String bomComponentUrl, Function<VersionBomComponentView, List<LinkableItem>> linkableItemExtractor) {
        return getBlackDuckDataHelper().getBomComponentView(bomComponentUrl)
                   .map(linkableItemExtractor)
                   .orElse(List.of());
    }

}
