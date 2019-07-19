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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentAttributeMap;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentKeys;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class SummaryMessageContentProcessor extends MessageContentProcessor {
    private final MessageOperationCombiner messageOperationCombiner;

    @Autowired
    public SummaryMessageContentProcessor(final MessageOperationCombiner messageOperationCombiner) {
        super(FormatType.SUMMARY);
        this.messageOperationCombiner = messageOperationCombiner;
    }

    @Override
    public List<MessageContentGroup> process(final List<ProviderMessageContent> messages) {
        final List<ProviderMessageContent> collapsedMessages = messageOperationCombiner.combine(messages);

        final List<MessageContentGroup> newGroups = new ArrayList<>();

        for (final ProviderMessageContent message : collapsedMessages) {
            final ProviderMessageContent summarizedMessage = summarize(message);
            newGroups
                .stream()
                .filter(group -> group.applies(summarizedMessage))
                .findAny()
                .ifPresentOrElse(group -> group.add(summarizedMessage), () -> newGroups.add(MessageContentGroup.singleton(summarizedMessage)));
        }
        return newGroups;
    }

    private ProviderMessageContent summarize(final ProviderMessageContent message) {
        final Set<ComponentItem> originalComponentItems = message.getComponentItems();
        if (null == originalComponentItems) {
            return message;
        }

        final Map<ItemOperation, LinkedHashSet<ComponentItem>> itemsByOperation = sortByOperation(originalComponentItems);

        final Set<ComponentItem> summarizedComponentItems = new LinkedHashSet<>();
        for (final LinkedHashSet<ComponentItem> componentSet : itemsByOperation.values()) {
            final LinkedHashSet<ComponentItem> summarizedComponentItemsForOperation = createSummarizedComponentItems(componentSet);
            summarizedComponentItems.addAll(summarizedComponentItemsForOperation);
        }

        try {
            return messageOperationCombiner.createNewMessage(message, summarizedComponentItems);
        } catch (AlertException e) {
            // If this happens, it means there is a bug in the Collector logic.
            throw new AlertRuntimeException(e);
        }
    }

    private Map<ItemOperation, LinkedHashSet<ComponentItem>> sortByOperation(final Set<ComponentItem> originalComponentItems) {
        final Map<ItemOperation, LinkedHashSet<ComponentItem>> itemsByOperation = new LinkedHashMap<>();
        for (final ComponentItem componentItem : originalComponentItems) {
            itemsByOperation.computeIfAbsent(componentItem.getOperation(), ignored -> new LinkedHashSet<>()).add(componentItem);
        }
        return itemsByOperation;
    }

    private LinkedHashSet<ComponentItem> createSummarizedComponentItems(final Set<ComponentItem> componentItemsForOperation) {
        final LinkedHashSet<ComponentItem> summarizedCategoryItems = new LinkedHashSet<>();
        final Map<String, ComponentAttributeMap> itemsByShallowKey = collectComponentItemData(componentItemsForOperation);
        for (final ComponentItem componentItem : componentItemsForOperation) {
            String summaryKey = createSummaryKey(componentItem.getComponentKeys(), componentItem.getItemsOfSameName());
            final SortedSet<LinkableItem> summarizedLinkableItems = createSummarizedLinkableItems(itemsByShallowKey.get(summaryKey));
            try {
                ComponentItem newComponentItem = messageOperationCombiner.createNewComponentItem(componentItem, summarizedLinkableItems);
                summarizedCategoryItems.add(newComponentItem);
            } catch (AlertException e) {
                // If this happens, it means there is a bug in the Collector logic.
                throw new AlertRuntimeException(e);
            }
        }

        return summarizedCategoryItems;
    }

    private Map<String, ComponentAttributeMap> collectComponentItemData(Set<ComponentItem> componentItemsForOperation) {
        Map<String, ComponentAttributeMap> itemsByCategory = new LinkedHashMap<>();
        for (final ComponentItem categoryItem : componentItemsForOperation) {
            ComponentAttributeMap itemsOfSameName = categoryItem.getItemsOfSameName();
            String summaryKey = createSummaryKey(categoryItem.getComponentKeys(), itemsOfSameName);

            ComponentAttributeMap allItemsForSpecialKey = itemsByCategory.computeIfAbsent(summaryKey, ignored -> new ComponentAttributeMap());
            allItemsForSpecialKey.putAll(itemsOfSameName);
        }

        return itemsByCategory;
    }

    private String createSummaryKey(ComponentKeys componentKeys, ComponentAttributeMap itemsOfSameName) {
        StringBuilder summaryKeyBuilder = new StringBuilder();
        summaryKeyBuilder.append(componentKeys.getShallowKey());
        for (List<LinkableItem> namedItems : itemsOfSameName.values()) {
            if (namedItems.size() == 1) {
                final LinkableItem item = namedItems
                                              .stream()
                                              .findAny()
                                              .orElseThrow();
                if (!item.isCollapsible() && item.isPartOfKey()) {
                    summaryKeyBuilder.append(item.getName());
                    summaryKeyBuilder.append(item.getValue());
                }
            }
        }

        return summaryKeyBuilder.toString();
    }

    private SortedSet<LinkableItem> createSummarizedLinkableItems(ComponentAttributeMap componentAttributeMap) {
        final SortedSet<LinkableItem> summarizedLinkableItems = new TreeSet<>();
        for (final Map.Entry<String, List<LinkableItem>> similarItemEntries : componentAttributeMap.entrySet()) {
            similarItemEntries.getValue()
                .stream()
                .findAny()
                .filter(LinkableItem::isSummarizable)
                .ifPresent(item -> createNewItems(item, summarizedLinkableItems, similarItemEntries.getKey(), similarItemEntries.getValue()));
        }

        return summarizedLinkableItems;
    }

    private void createNewItems(final LinkableItem item, final Set<LinkableItem> summarizedLinkableItems, final String oldItemName, final Collection<LinkableItem> linkableItems) {
        final boolean isCountable = item.isCountable();
        final boolean isNumericValue = item.isNumericValue();
        final String newItemName = generateSummaryItemName(oldItemName, isCountable, isNumericValue);

        if (isCountable) {
            final String newItemValue = generateCountAsString(oldItemName, linkableItems, isNumericValue);
            final LinkableItem newLinkableItem = new LinkableItem(newItemName, newItemValue);
            updateSummarizability(newLinkableItem, true, true);
            summarizedLinkableItems.add(newLinkableItem);
        } else {
            final Set<LinkableItem> newDetailedItems = createLinkableItemsByValue(newItemName, linkableItems);
            if (newDetailedItems.isEmpty()) {
                final LinkableItem summarizedLinkableItem = new LinkableItem(newItemName, item.getValue());
                updateSummarizability(summarizedLinkableItem, false, isNumericValue);
                summarizedLinkableItems.add(summarizedLinkableItem);
            } else {
                summarizedLinkableItems.addAll(newDetailedItems);
            }
        }
    }

    private String generateSummaryItemName(final String oldItemName, final boolean isCountable, final boolean isNumeric) {
        if (isCountable) {
            if (isNumeric) {
                return String.format("Total %s Count", oldItemName);
            }
            return String.format("%s Count", oldItemName);
        }
        return oldItemName;
    }

    private LinkedHashSet<LinkableItem> createLinkableItemsByValue(final String itemName, final Collection<LinkableItem> linkableItems) {
        final Set<String> uniqueValuesMatchingName = linkableItems
                                                         .stream()
                                                         .filter(item -> itemName.equals(item.getName()))
                                                         .map(LinkableItem::getValue)
                                                         .collect(Collectors.toSet());
        final LinkedHashSet<LinkableItem> summarizedLinkableItems = new LinkedHashSet<>();
        for (final String uniqueValue : uniqueValuesMatchingName) {
            final LinkableItem newLinkableItem = new LinkableItem(itemName, uniqueValue);
            newLinkableItem.setSummarizable(true);
            summarizedLinkableItems.add(newLinkableItem);
        }
        return summarizedLinkableItems;
    }

    private String generateCountAsString(final String itemName, final Collection<LinkableItem> items, final boolean isNumericValue) {
        final long count = generateCount(itemName, items, isNumericValue);
        return Long.toString(count);
    }

    private long generateCount(final String itemName, final Collection<LinkableItem> items, final boolean isNumericValue) {
        if (isNumericValue) {
            long count = 0;
            for (final LinkableItem item : items) {
                if (itemName.equals(item.getName())) {
                    final String value = item.getValue();
                    if (StringUtils.isNumeric(value)) {
                        final int numericValue = Integer.parseInt(value);
                        count += numericValue;
                    }
                }
            }
            return count;
        }

        return items
                   .stream()
                   .filter(item -> itemName.equals(item.getName()))
                   .count();
    }

    private void updateSummarizability(final LinkableItem item, final boolean isCountable, final boolean isNumeric) {
        item.setCountable(isCountable);
        item.setNumericValueFlag(isNumeric);
        item.setSummarizable(true);
    }

}
