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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.processor2.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

public abstract class BlackDuckPolicyCollector extends BlackDuckCollector {
    public static final String CATEGORY_TYPE = "policy";
    private Logger logger = LoggerFactory.getLogger(getClass());

    public BlackDuckPolicyCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final Collection<ProviderContentType> contentTypes, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, messageContentProcessorList, contentTypes, blackDuckProperties);
    }

    protected Optional<ComponentItem> addApplicableItems(Long notificationId, LinkableItem componentLinkableItem, LinkableItem componentVersionItem, Set<LinkableItem> policyItems, ItemOperation operation) {
        try {
            updatePolicyItems(policyItems);
            ComponentItem.Builder builder = new ComponentItem.Builder();
            builder.applyComponentData(componentLinkableItem)
                .applySubComponent(componentVersionItem)
                .applyAllComponentAttributes(policyItems)
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

    private void updatePolicyItems(final Set<LinkableItem> policyItems) {
        policyItems.forEach(this::updatePolicyItem);
    }

    private void updatePolicyItem(final LinkableItem policyItem) {
        policyItem.setCollapsible(true);
        policyItem.setCountable(true);
        policyItem.setSummarizable(true);
    }

}
