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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class SummaryMessageContentProcessor extends MessageContentProcessor {
    private final DigestMessageContentProcessor digestMessageContentProcessor;

    @Autowired
    public SummaryMessageContentProcessor(final DigestMessageContentProcessor digestMessageContentProcessor) {
        super(FormatType.SUMMARY);
        this.digestMessageContentProcessor = digestMessageContentProcessor;
    }

    @Override
    public List<AggregateMessageContent> process(final List<AggregateMessageContent> messages) {
        final List<AggregateMessageContent> digestMessages = digestMessageContentProcessor.process(messages);
        return digestMessages
                   .stream()
                   .map(this::summarize)
                   .collect(Collectors.toList());
    }

    private AggregateMessageContent summarize(final AggregateMessageContent message) {
        final SortedSet<CategoryItem> originalCategoryItems = message.getCategoryItems();
        if (null == originalCategoryItems) {
            return message;
        }

        final Map<ItemOperation, LinkedHashSet<CategoryItem>> itemsByOperation = sortByOperation(originalCategoryItems);

        final SortedSet<CategoryItem> summarizedCategoryItems = new TreeSet<>();
        for (final Map.Entry<ItemOperation, LinkedHashSet<CategoryItem>> sortedEntry : itemsByOperation.entrySet()) {
            final LinkedHashSet<CategoryItem> summarizedCategoryItemsForOperation = createSummarizedCategoryItems(sortedEntry.getKey(), sortedEntry.getValue());
            summarizedCategoryItems.addAll(summarizedCategoryItemsForOperation);
        }

        return new AggregateMessageContent(message.getName(), message.getValue(), message.getUrl().orElse(null), message.getSubTopic().orElse(null), summarizedCategoryItems);
    }

    private Map<ItemOperation, LinkedHashSet<CategoryItem>> sortByOperation(final Set<CategoryItem> originalCategoryItems) {
        final Map<ItemOperation, LinkedHashSet<CategoryItem>> itemsByOperation = new LinkedHashMap<>();
        for (final CategoryItem categoryItem : originalCategoryItems) {
            itemsByOperation.computeIfAbsent(categoryItem.getOperation(), ignored -> new LinkedHashSet<>()).add(categoryItem);
        }
        return itemsByOperation;
    }

    private LinkedHashSet<CategoryItem> createSummarizedCategoryItems(final ItemOperation operation, final Set<CategoryItem> categoryItemsForOperation) {
        final List<CategoryItem> summarizedCategoryItems = new LinkedList<>();
        for (final CategoryItem categoryItem : categoryItemsForOperation) {
            final SortedSet<LinkableItem> summarizedLinkableItems = createSummarizedLinkableItems(categoryItemsForOperation, categoryItem.getItems());
            final CategoryKey categoryKey = createCategoryKeyFromLinkableItems(summarizedLinkableItems);
            final CategoryItem newCategoryItem = new CategoryItem(categoryKey, operation, categoryItem.getNotificationId(), summarizedLinkableItems);
            summarizedCategoryItems.add(newCategoryItem);
        }

        return collapseDuplicateCategoryItems(summarizedCategoryItems);
    }

    private SortedSet<LinkableItem> createSummarizedLinkableItems(final Set<CategoryItem> categoryItems, final Set<LinkableItem> linkableItems) {
        final Map<String, List<LinkableItem>> itemsOfSameName = new LinkedHashMap<>();
        categoryItems.forEach(item -> itemsOfSameName.putAll(item.getItemsOfSameName()));

        final SortedSet<LinkableItem> summarizedLinkableItems = new TreeSet<>();
        for (final Map.Entry<String, List<LinkableItem>> similarItemEntries : itemsOfSameName.entrySet()) {
            similarItemEntries.getValue()
                .stream()
                .findAny()
                .filter(LinkableItem::isSummarizable)
                .ifPresent(item -> createNewItems(item, summarizedLinkableItems, similarItemEntries.getKey(), linkableItems));
        }

        return summarizedLinkableItems;
    }

    private void createNewItems(final LinkableItem item, final Set<LinkableItem> summarizedLinkableItems, final String oldItemName, final Set<LinkableItem> linkableItems) {
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

    private LinkedHashSet<LinkableItem> createLinkableItemsByValue(final String itemName, final Set<LinkableItem> linkableItems) {
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

    private LinkedHashSet<CategoryItem> collapseDuplicateCategoryItems(final List<CategoryItem> categoryItems) {
        final LinkedHashMap<CategoryKey, CategoryItem> keyToItem = new LinkedHashMap<>();
        for (final CategoryItem currentItem : categoryItems) {
            CategoryItem updatedCategoryItem = currentItem;
            final CategoryKey categoryKey = currentItem.getCategoryKey();
            if (keyToItem.containsKey(categoryKey)) {
                final CategoryItem oldCategoryItem = keyToItem.get(categoryKey);
                final Set<LinkableItem> oldLinkableItems = oldCategoryItem.getItems();
                final Set<LinkableItem> currentLinkableItems = currentItem.getItems();
                final List<LinkableItem> combinedLinkableItems = Stream.concat(oldLinkableItems.stream(), currentLinkableItems.stream()).collect(Collectors.toList());

                final SortedSet<LinkableItem> collapsedLinkableItems = collapseDuplicateLinkableItems(combinedLinkableItems);
                updatedCategoryItem = new CategoryItem(categoryKey, currentItem.getOperation(), currentItem.getNotificationId(), collapsedLinkableItems);
            }
            keyToItem.put(categoryKey, updatedCategoryItem);
        }
        return new LinkedHashSet<>(keyToItem.values());
    }

    private SortedSet<LinkableItem> collapseDuplicateLinkableItems(final List<LinkableItem> linkableItems) {
        final LinkedHashMap<String, List<LinkableItem>> nameToItems = new LinkedHashMap<>();
        for (final LinkableItem item : linkableItems) {
            nameToItems.computeIfAbsent(item.getName(), ignored -> new LinkedList<>()).add(item);
        }

        final SortedSet<LinkableItem> collapsedItems = new TreeSet<>();
        for (final Map.Entry<String, List<LinkableItem>> nameToItemEntries : nameToItems.entrySet()) {
            final String itemName = nameToItemEntries.getKey();
            final List<LinkableItem> itemsOfSameName = nameToItemEntries.getValue();
            final boolean isCountable = itemsOfSameName
                                            .stream()
                                            .anyMatch(LinkableItem::isCountable);
            if (isCountable) {
                final boolean isNumeric = itemsOfSameName
                                              .stream()
                                              .anyMatch(LinkableItem::isNumericValue);
                final String countAsString = generateCountAsString(itemName, itemsOfSameName, isNumeric);
                final LinkableItem collapsedItem = new LinkableItem(itemName, countAsString);
                updateSummarizability(collapsedItem, true, isNumeric);
                collapsedItems.add(collapsedItem);
            } else {
                collapsedItems.addAll(itemsOfSameName);
            }
        }
        return collapsedItems;
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

    private CategoryKey createCategoryKeyFromLinkableItems(final Collection<LinkableItem> linkableItems) {
        final List<String> itemNameValueSequence = new LinkedList<>();
        for (final LinkableItem item : linkableItems) {
            if (!item.isSummarizable()) {
                continue;
            }
            itemNameValueSequence.add(item.getName());
            if (!item.isNumericValue()) {
                itemNameValueSequence.add(item.getValue());
            }
        }
        return CategoryKey.from("summary", itemNameValueSequence);
    }

    private void updateSummarizability(final LinkableItem item, final boolean isCountable, final boolean isNumeric) {
        item.setCountable(isCountable);
        item.setNumericValueFlag(isNumeric);
        item.setSummarizable(true);
    }

}
