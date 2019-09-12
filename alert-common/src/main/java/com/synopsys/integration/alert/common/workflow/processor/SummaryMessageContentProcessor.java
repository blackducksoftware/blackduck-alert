/**
 * alert-common
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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentAttributeMap;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class SummaryMessageContentProcessor extends MessageContentProcessor {
    public static final String COMPONENT_ITEM_NAME_SUMMARY_VALUE = "Summary";
    public static final String COMPONENT_ITEM_NAME_SUMMARY_LABEL = "Format";
    public static final String COMPONENT_ITEM_NAME_COMPONENTS = "Components";
    private final MessageOperationCombiner messageOperationCombiner;

    @Autowired
    public SummaryMessageContentProcessor(MessageOperationCombiner messageOperationCombiner) {
        super(FormatType.SUMMARY);
        this.messageOperationCombiner = messageOperationCombiner;
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        List<ProviderMessageContent> collapsedMessages = messageOperationCombiner.combine(messages);

        List<MessageContentGroup> newGroups = new ArrayList<>();

        for (ProviderMessageContent message : collapsedMessages) {
            ProviderMessageContent summarizedMessage = summarize(message);
            newGroups
                .stream()
                .filter(group -> group.applies(summarizedMessage))
                .findAny()
                .ifPresentOrElse(group -> group.add(summarizedMessage), () -> newGroups.add(MessageContentGroup.singleton(summarizedMessage)));
        }
        return newGroups;
    }

    private ProviderMessageContent summarize(ProviderMessageContent message) {
        Set<ComponentItem> originalComponentItems = message.getComponentItems();
        if (null == originalComponentItems) {
            return message;
        }

        Map<ItemOperation, LinkedHashSet<ComponentItem>> itemsByOperation = sortByOperation(originalComponentItems);

        Set<ComponentItem> summarizedComponentItems = new LinkedHashSet<>();
        for (LinkedHashSet<ComponentItem> componentSet : itemsByOperation.values()) {
            LinkedHashSet<ComponentItem> summarizedComponentCounts = summarizeCategoryComponentCounts(componentSet);
            summarizedComponentItems.addAll(summarizedComponentCounts);
        }

        try {
            return messageOperationCombiner.createNewMessage(message, summarizedComponentItems);
        } catch (AlertException e) {
            // If this happens, it means there is a bug in the Collector logic.
            throw new AlertRuntimeException(e);
        }
    }

    private Map<ItemOperation, LinkedHashSet<ComponentItem>> sortByOperation(Set<ComponentItem> originalComponentItems) {
        Map<ItemOperation, LinkedHashSet<ComponentItem>> itemsByOperation = new LinkedHashMap<>();
        for (ComponentItem componentItem : originalComponentItems) {
            itemsByOperation.computeIfAbsent(componentItem.getOperation(), ignored -> new LinkedHashSet<>()).add(componentItem);
        }
        return itemsByOperation;
    }

    private LinkedHashSet<ComponentItem> summarizeCategoryComponentCounts(Set<ComponentItem> componentItems) {
        // Organize the items by category such as policy and vulnerability.
        // Then have the item priority with the set of components.
        Map<String, Map<ComponentItemPriority, Set<ComponentItem>>> itemsByCategory = new LinkedHashMap<>();

        for (ComponentItem item : componentItems) {
            Map<ComponentItemPriority, Set<ComponentItem>> itemsByPriority = itemsByCategory.computeIfAbsent(item.getCategory(), ignored -> new LinkedHashMap<>());
            Set<ComponentItem> itemList = itemsByPriority.computeIfAbsent(item.getPriority(), ignored -> new LinkedHashSet<>());
            itemList.add(item);
        }

        LinkedHashSet<ComponentItem> componentSummaries = new LinkedHashSet<>();
        for (Map.Entry<String, Map<ComponentItemPriority, Set<ComponentItem>>> entry : itemsByCategory.entrySet()) {
            for (Map.Entry<ComponentItemPriority, Set<ComponentItem>> priorityEntries : entry.getValue().entrySet()) {
                try {
                    Set<ComponentItem> items = priorityEntries.getValue();

                    List<LinkableItem> summarizableAttributes = items.stream()
                                                                    .map(ComponentItem::getComponentAttributes)
                                                                    .flatMap(Set::stream)
                                                                    .filter(LinkableItem::isSummarizable)
                                                                    .collect(Collectors.toList());
                    if (!summarizableAttributes.isEmpty()) {
                        Set<Long> notificationIds = items.stream()
                                                        .map(ComponentItem::getNotificationIds)
                                                        .flatMap(Set::stream)
                                                        .collect(Collectors.toSet());
                        ItemOperation operation = items.stream().map(ComponentItem::getOperation).findFirst().orElse(ItemOperation.UPDATE);

                        ComponentItem.Builder builder = new ComponentItem.Builder();
                        builder.applyNotificationIds(notificationIds);
                        builder.applyPriority(priorityEntries.getKey());
                        builder.applyCategory(entry.getKey());
                        builder.applyComponentData(COMPONENT_ITEM_NAME_SUMMARY_LABEL, COMPONENT_ITEM_NAME_SUMMARY_VALUE);
                        builder.applyOperation(operation);

                        Map<String, Long> componentCounts = items.stream()
                                                                .map(ComponentItem::createKey)
                                                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                        LinkableItem countItem = new LinkableItem(COMPONENT_ITEM_NAME_COMPONENTS, String.valueOf(componentCounts.keySet().size()));
                        countItem.setCollapsible(true);
                        countItem.setCountable(true);
                        countItem.setSummarizable(true);
                        builder.applyComponentAttribute(countItem);

                        final Collection<LinkableItem> summarizedLinkableItems = summarizeComponentItemData(summarizableAttributes);
                        builder.applyAllComponentAttributes(summarizedLinkableItems);
                        componentSummaries.add(builder.build());
                    }
                } catch (AlertException ex) {
                    throw new AlertRuntimeException(ex);
                }
            }
        }
        return componentSummaries;
    }

    private Collection<LinkableItem> summarizeComponentItemData(Collection<LinkableItem> attributes) {
        if (attributes.isEmpty()) {
            return List.of();
        }
        ComponentAttributeMap attributeMap = new ComponentAttributeMap();
        for (LinkableItem item : attributes) {
            String name = item.getName();
            attributeMap.computeIfAbsent(name, ignored -> new LinkedList<>()).add(item);
        }
        return createSummarizedLinkableItems(attributeMap);
    }

    private List<LinkableItem> createSummarizedLinkableItems(ComponentAttributeMap componentAttributeMap) {
        final List<LinkableItem> summarizedLinkableItems = new LinkedList<>();
        for (Map.Entry<String, List<LinkableItem>> similarItemEntries : componentAttributeMap.entrySet()) {
            similarItemEntries.getValue()
                .stream()
                .findAny()
                .filter(LinkableItem::isSummarizable)
                .ifPresent(item -> createNewItems(item, summarizedLinkableItems, similarItemEntries.getKey(), similarItemEntries.getValue()));
        }

        return summarizedLinkableItems.stream().sorted().collect(Collectors.toList());
    }

    private void createNewItems(LinkableItem item, Collection<LinkableItem> summarizedLinkableItems, String oldItemName, Collection<LinkableItem> linkableItems) {
        boolean isCountable = item.isCountable();
        boolean isNumericValue = item.isNumericValue();
        String newItemName = generateSummaryItemName(oldItemName, isCountable, isNumericValue);

        if (isCountable) {
            String newItemValue = generateCountAsString(oldItemName, linkableItems, isNumericValue);
            LinkableItem newLinkableItem = new LinkableItem(newItemName, newItemValue);
            updateSummarizability(newLinkableItem, true, true);
            summarizedLinkableItems.add(newLinkableItem);
        } else {
            Collection<LinkableItem> newDetailedItems = createLinkableItemsByValue(newItemName, linkableItems);
            if (newDetailedItems.isEmpty()) {
                final LinkableItem summarizedLinkableItem = new LinkableItem(newItemName, item.getValue());
                updateSummarizability(summarizedLinkableItem, false, isNumericValue);
                summarizedLinkableItems.add(summarizedLinkableItem);
            } else {
                summarizedLinkableItems.addAll(newDetailedItems);
            }
        }
    }

    private String generateSummaryItemName(String oldItemName, boolean isCountable, boolean isNumeric) {
        if (isCountable) {
            if (isNumeric) {
                return String.format("Total %s Count", oldItemName);
            }
            return String.format("%s Count", oldItemName);
        }
        return oldItemName;
    }

    private List<LinkableItem> createLinkableItemsByValue(String itemName, Collection<LinkableItem> linkableItems) {
        List<String> uniqueValuesMatchingName = linkableItems
                                                    .stream()
                                                    .filter(item -> itemName.equals(item.getName()))
                                                    .map(LinkableItem::getValue)
                                                    .collect(Collectors.toList());
        List<LinkableItem> summarizedLinkableItems = new LinkedList<>();
        for (String uniqueValue : uniqueValuesMatchingName) {
            LinkableItem newLinkableItem = new LinkableItem(itemName, uniqueValue);
            newLinkableItem.setSummarizable(true);
            summarizedLinkableItems.add(newLinkableItem);
        }
        return summarizedLinkableItems;
    }

    private String generateCountAsString(String itemName, Collection<LinkableItem> items, boolean isNumericValue) {
        long count = generateCount(itemName, items, isNumericValue);
        return Long.toString(count);
    }

    private long generateCount(String itemName, Collection<LinkableItem> items, boolean isNumericValue) {
        if (isNumericValue) {
            long count = 0;
            for (LinkableItem item : items) {
                if (itemName.equals(item.getName())) {
                    String value = item.getValue();
                    if (StringUtils.isNumeric(value)) {
                        int numericValue = Integer.parseInt(value);
                        count += numericValue;
                    }
                }
            }
            return count;
        }

        return items
                   .stream()
                   .filter(item -> itemName.equals(item.getName()))
                   .collect(Collectors.toSet())
                   .size();
    }

    private void updateSummarizability(LinkableItem item, boolean isCountable, boolean isNumeric) {
        item.setCountable(isCountable);
        item.setNumericValueFlag(isNumeric);
        item.setSummarizable(true);
    }

}
