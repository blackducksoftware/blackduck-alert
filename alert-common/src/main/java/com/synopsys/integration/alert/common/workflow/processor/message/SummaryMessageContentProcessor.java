/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.processor.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.combiner.MessageOperationCombiner;
import com.synopsys.integration.alert.common.workflow.combiner.TopLevelActionCombiner;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class SummaryMessageContentProcessor extends MessageContentProcessor {
    public static final String COMPONENT_ITEM_NAME_SUMMARY_VALUE = "Summary";
    public static final String COMPONENT_ITEM_NAME_SUMMARY_LABEL = "Format";

    private final Logger logger = LoggerFactory.getLogger(SummaryMessageContentProcessor.class);
    private final TopLevelActionCombiner topLevelActionCombiner;
    private final MessageOperationCombiner messageOperationCombiner;

    @Autowired
    public SummaryMessageContentProcessor(TopLevelActionCombiner topLevelActionCombiner, MessageOperationCombiner messageOperationCombiner) {
        super(ProcessingType.SUMMARY);
        this.topLevelActionCombiner = topLevelActionCombiner;
        this.messageOperationCombiner = messageOperationCombiner;
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        List<ProviderMessageContent> messagesCombinedAtTopLevel = topLevelActionCombiner.combine(messages);
        List<ProviderMessageContent> messagesCombinedAtComponentLevel = messageOperationCombiner.combine(messagesCombinedAtTopLevel);

        List<MessageContentGroup> newGroups = new ArrayList<>();
        for (ProviderMessageContent message : messagesCombinedAtComponentLevel) {
            ProviderMessageContent summarizedMessage = summarizeMessageContent(message);

            if (filterEmptyContent(summarizedMessage)) {
                newGroups
                    .stream()
                    .filter(group -> group.applies(summarizedMessage))
                    .findAny()
                    .ifPresentOrElse(group -> group.add(summarizedMessage), () -> newGroups.add(MessageContentGroup.singleton(summarizedMessage)));
            }
        }
        return newGroups;
    }

    private ProviderMessageContent summarizeMessageContent(ProviderMessageContent messageContent) {
        SetMap<String, ComponentItem> groupedItems = sortByCategoryOperationPriorityGrouping(messageContent.getComponentItems());

        Set<ComponentItem> summarizedComponentItems = new LinkedHashSet<>();
        for (Set<ComponentItem> relatedComponentItems : groupedItems.values()) {
            Set<ComponentItem> summarizedRelatedItems = summarizeComponentItems(relatedComponentItems);
            summarizedComponentItems.addAll(summarizedRelatedItems);
        }

        try {
            List<ComponentItem> sortedComponentItems = summarizedComponentItems
                                                           .stream()
                                                           .sorted(ComponentItem.createDefaultComparator())
                                                           .collect(Collectors.toList());
            return messageOperationCombiner.createNewMessage(messageContent, sortedComponentItems);
        } catch (AlertException e) {
            // If this happens, it means there is a bug in the Collector logic.
            throw new AlertRuntimeException(e);
        }
    }

    private SetMap<String, ComponentItem> sortByCategoryOperationPriorityGrouping(Set<ComponentItem> originalComponentItems) {
        SetMap<String, ComponentItem> itemsByOperation = SetMap.createDefault();
        for (ComponentItem componentItem : originalComponentItems) {
            if (ItemOperation.INFO == componentItem.getOperation()) {
                // INFO messages are usually to follow up on a previous message with additional attributes, and therefore irrelevant to Summary Format.
                continue;
            }
            StringBuilder keyBuilder = new StringBuilder()
                                           .append(componentItem.getCategory())
                                           .append(componentItem.getOperation().name())
                                           .append(componentItem.getPriority().name());

            String groupString = componentItem
                                     .getSeverity()
                                     .map(item -> item.getLabel() + item.getValue())
                                     .orElse("DEFAULT_GROUPING_STRING");
            keyBuilder.append(groupString);

            itemsByOperation.add(keyBuilder.toString(), componentItem);
        }
        return itemsByOperation;
    }

    private Set<ComponentItem> summarizeComponentItems(Collection<ComponentItem> itemsToSummarize) {
        Optional<ComponentItem> optionalArbitraryItem = itemsToSummarize
                                                            .stream()
                                                            .findAny();

        Set<ComponentItem> summarizedItems = new HashSet<>();
        if (optionalArbitraryItem.isPresent()) {
            ComponentItem arbitraryComponent = optionalArbitraryItem.get();
            Set<LinkableItem> categoryItems = itemsToSummarize
                                                  .stream()
                                                  .map(ComponentItem::getCategoryItem)
                                                  .collect(Collectors.toSet());
            Set<Long> notificationIds = itemsToSummarize
                                            .stream()
                                            .map(ComponentItem::getNotificationIds)
                                            .flatMap(Set::stream)
                                            .collect(Collectors.toSet());

            LinkableItem countItem = generateCountItem(arbitraryComponent.getCategory(), categoryItems);
            ComponentItem.Builder builder = new ComponentItem.Builder()
                                                .applyCategory(arbitraryComponent.getCategory())
                                                .applyOperation(arbitraryComponent.getOperation())
                                                .applyPriority(arbitraryComponent.getPriority())
                                                .applyComponentData(COMPONENT_ITEM_NAME_SUMMARY_LABEL, COMPONENT_ITEM_NAME_SUMMARY_VALUE)
                                                .applyCategoryItem(countItem)
                                                .applyCollapseOnCategory(false)
                                                .applySeverity(arbitraryComponent.getSeverity().orElse(null))
                                                .applyNotificationIds(notificationIds);
            try {
                ComponentItem summarizedItem = builder.build();
                summarizedItems.add(summarizedItem);
            } catch (AlertException e) {
                logger.warn("Failed to summarize ComponentItem. Category: {}, Operation: {}, {}: {}",
                    arbitraryComponent.getCategory(), arbitraryComponent.getOperation(), arbitraryComponent.getComponent().getLabel(), arbitraryComponent.getComponent().getValue());
            }
        }
        return summarizedItems;
    }

    private LinkableItem generateCountItem(String category, Collection<LinkableItem> items) {
        boolean isNumericValue = items
                                     .stream()
                                     .allMatch(LinkableItem::isNumericValue);
        long count = generateCount(items, isNumericValue);
        String countString = Long.toString(count);

        LinkableItem countItem = new LinkableItem(category + " Count", countString);
        countItem.setNumericValueFlag(true);
        return countItem;
    }

    private long generateCount(Collection<LinkableItem> items, boolean isNumericValue) {
        if (isNumericValue) {
            long count = 0;
            for (LinkableItem item : items) {
                String value = item.getValue();
                if (StringUtils.isNumeric(value)) {
                    int numericValue = Integer.parseInt(value);
                    count += numericValue;
                }
            }
            return count;
        }

        return items.size();
    }

}
