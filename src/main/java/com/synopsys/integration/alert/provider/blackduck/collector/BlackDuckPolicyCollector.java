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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public abstract class BlackDuckPolicyCollector extends BlackDuckCollector {
    public static final String CATEGORY_TYPE = "policy";
    private final Map<String, ComponentItemPriority> priorityMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public BlackDuckPolicyCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final Collection<ProviderContentType> contentTypes, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, messageContentProcessorList, contentTypes, blackDuckProperties);

        priorityMap.put("blocker", ComponentItemPriority.HIGH);
        priorityMap.put("critical", ComponentItemPriority.HIGH);
        priorityMap.put("major", ComponentItemPriority.HIGH);
        priorityMap.put("minor", ComponentItemPriority.MEDIUM);
        priorityMap.put("trivial", ComponentItemPriority.LOW);
        priorityMap.put("unspecified", ComponentItemPriority.STANDARD);
    }

    protected Optional<ComponentItem> addApplicableItems(Long notificationId, LinkableItem componentLinkableItem, LinkableItem componentVersionItem, Collection<LinkableItem> policyItems, ItemOperation operation,
        ComponentItemPriority priority) {
        try {
            ComponentItem.Builder builder = new ComponentItem.Builder();
            builder.applyComponentData(componentLinkableItem)
                .applySubComponent(componentVersionItem)
                .applyAllComponentAttributes(policyItems)
                .applyPriority(priority)
                .applyCategory(CATEGORY_TYPE)
                .applyOperation(operation)
                .applyNotificationId(notificationId);

            return Optional.of(builder.build());
        } catch (Exception ex) {
            logger.info("Error building policy component for notification {}, operation {}, component {}, component version {}", notificationId, operation, componentLinkableItem);
            logger.error("Error building policy component cause ", ex);
            return Optional.empty();
        }
    }

    protected LinkableItem createPolicyLinkableItem(final PolicyInfo policyInfo) {
        final String policyName = policyInfo.getPolicyName();
        final String severity = policyInfo.getSeverity();
        String displayName = policyName;
        if (StringUtils.isNotBlank(severity)) {
            displayName = String.format("%s (%s)", policyName, severity);
        }
        final LinkableItem linkableItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, displayName, null);
        linkableItem.setCollapsible(true);
        linkableItem.setSummarizable(true);
        linkableItem.setCountable(true);
        return linkableItem;
    }

    protected ComponentItemPriority mapSeverityToPriority(String severity) {
        if (StringUtils.isBlank(severity) || !priorityMap.containsKey(severity.trim().toLowerCase())) {
            return ComponentItemPriority.STANDARD;
        }
        return priorityMap.get(severity.trim().toLowerCase());
    }

}
