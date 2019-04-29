package com.synopsys.integration.alert.common.workflow.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
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

        final Map<ItemOperation, SortedSet<CategoryItem>> itemsByOperation = sortByOperation(originalCategoryItems);

        final SortedSet<CategoryItem> summarizedCategoryItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<ItemOperation, SortedSet<CategoryItem>> sortedEntry : itemsByOperation.entrySet()) {
            final SortedSet<CategoryItem> summarizedCategoryItemsForOperation = createSummarizedCategoryItems(sortedEntry.getKey(), sortedEntry.getValue());
            summarizedCategoryItems.addAll(summarizedCategoryItemsForOperation);
        }

        return new AggregateMessageContent(message.getName(), message.getValue(), message.getUrl().orElse(null), message.getSubTopic().orElse(null), summarizedCategoryItems);
    }

    private Map<ItemOperation, SortedSet<CategoryItem>> sortByOperation(final SortedSet<CategoryItem> originalCategoryItems) {
        final Map<ItemOperation, SortedSet<CategoryItem>> itemsByOperation = new LinkedHashMap<>();
        for (final CategoryItem categoryItem : originalCategoryItems) {
            itemsByOperation.computeIfAbsent(categoryItem.getOperation(), ignored -> new ConcurrentSkipListSet<>()).add(categoryItem);
        }
        return itemsByOperation;
    }

    private SortedSet<CategoryItem> createSummarizedCategoryItems(final ItemOperation operation, final SortedSet<CategoryItem> categoryItemsForOperation) {
        final List<CategoryItem> summarizedCategoryItems = new LinkedList<>();
        for (final CategoryItem categoryItem : categoryItemsForOperation) {
            final SortedSet<LinkableItem> summarizedLinkableItems = createSummarizedLinkableItems(categoryItemsForOperation, categoryItem.getItems());
            final CategoryKey categoryKey = createCategoryKeyFromLinkableItems(summarizedLinkableItems);
            final CategoryItem newCategoryItem = new CategoryItem(categoryKey, operation, categoryItem.getNotificationId(), summarizedLinkableItems);
            summarizedCategoryItems.add(newCategoryItem);
        }

        return collapseDuplicateCategoryItems(summarizedCategoryItems);
    }

    private SortedSet<LinkableItem> createSummarizedLinkableItems(final SortedSet<CategoryItem> categoryItems, final SortedSet<LinkableItem> linkableItems) {
        final Map<String, List<LinkableItem>> itemsOfSameName = new ConcurrentSkipListMap<>();
        categoryItems.forEach(item -> itemsOfSameName.putAll(item.getItemsOfSameName()));

        final SortedSet<LinkableItem> summarizedLinkableItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<String, List<LinkableItem>> similarItemEntries : itemsOfSameName.entrySet()) {
            final Optional<LinkableItem> summarizableItem = similarItemEntries.getValue()
                                                                .stream()
                                                                .findAny()
                                                                .filter(LinkableItem::isSummarizable);
            summarizableItem.ifPresent(item -> createNewItems(item, summarizedLinkableItems, similarItemEntries.getKey(), linkableItems));
        }

        return summarizedLinkableItems;
    }

    private void createNewItems(final LinkableItem item, final SortedSet<LinkableItem> summarizedLinkableItems, final String oldItemName, final SortedSet<LinkableItem> linkableItems) {
        final boolean isCountable = item.isCountable();
        final boolean isNumericValue = item.isNumericValue();
        final String newItemName = generateSummaryItemName(oldItemName, isCountable, isNumericValue);

        if (isCountable) {
            final String newItemValue = generateCountAsString(oldItemName, linkableItems, isNumericValue);
            final LinkableItem newLinkableItem = new LinkableItem(newItemName, newItemValue);
            newLinkableItem.setCountable(true);
            newLinkableItem.setNumericValueFlag(isNumericValue);
            newLinkableItem.setSummarizable(true);
            summarizedLinkableItems.add(newLinkableItem);
        } else {
            final SortedSet<LinkableItem> newDetailedItems = createLinkableItemsByValue(newItemName, linkableItems);
            if (newDetailedItems.isEmpty()) {
                summarizedLinkableItems.add(new LinkableItem(newItemName, item.getValue()));
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

    private SortedSet<LinkableItem> createLinkableItemsByValue(final String itemName, final SortedSet<LinkableItem> linkableItems) {
        final Map<String, Integer> valueCounts = countItemsOfSameValueForName(itemName, linkableItems);
        final SortedSet<LinkableItem> summarizedLinkableItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<String, Integer> valueCount : valueCounts.entrySet()) {
            // TODO original approach to tackle vulnerability counts:
            //            final String newName = String.format("%s - %s", itemName, valueCount.getKey());
            //            final String newValue = Integer.toString(valueCount.getValue());
            //  summarizedLinkableItems.add(new LinkableItem(newName, newValue));

            final LinkableItem newLinkableItem = new LinkableItem(itemName, valueCount.getKey());
            newLinkableItem.setSummarizable(true);
            summarizedLinkableItems.add(newLinkableItem);
        }
        return summarizedLinkableItems;
    }

    private Map<String, Integer> countItemsOfSameValueForName(final String itemName, final SortedSet<LinkableItem> linkableItems) {
        final Map<String, Integer> valueCounts = new LinkedHashMap<>();
        for (final LinkableItem item : linkableItems) {
            if (itemName.equals(item.getName())) {
                final String value = item.getValue();
                final Integer valueCount = valueCounts.getOrDefault(value, 0);
                valueCounts.put(value, valueCount + 1);
            }
        }
        return valueCounts;
    }

    private SortedSet<CategoryItem> collapseDuplicateCategoryItems(final List<CategoryItem> categoryItems) {
        final ConcurrentSkipListMap<CategoryKey, CategoryItem> keyToItem = new ConcurrentSkipListMap<>();
        for (final CategoryItem currentItem : categoryItems) {
            CategoryItem updatedCategoryItem = currentItem;
            final CategoryKey categoryKey = currentItem.getCategoryKey();
            if (keyToItem.containsKey(categoryKey)) {
                final CategoryItem oldCategoryItem = keyToItem.get(categoryKey);
                final SortedSet<LinkableItem> oldLinkableItems = oldCategoryItem.getItems();
                final SortedSet<LinkableItem> currentLinkableItems = currentItem.getItems();
                final List<LinkableItem> combinedLinkableItems = Stream.concat(oldLinkableItems.stream(), currentLinkableItems.stream()).collect(Collectors.toList());

                final SortedSet<LinkableItem> collapsedLinkableItems = collapseDuplicateLinkableItems(combinedLinkableItems);
                updatedCategoryItem = new CategoryItem(categoryKey, currentItem.getOperation(), currentItem.getNotificationId(), collapsedLinkableItems);
            }
            keyToItem.put(categoryKey, updatedCategoryItem);
        }
        return new ConcurrentSkipListSet<>(keyToItem.values());
    }

    private SortedSet<LinkableItem> collapseDuplicateLinkableItems(final List<LinkableItem> linkableItems) {
        final ConcurrentSkipListMap<String, List<LinkableItem>> nameToItems = new ConcurrentSkipListMap<>();
        for (final LinkableItem item : linkableItems) {
            nameToItems.computeIfAbsent(item.getName(), ignored -> new LinkedList<>()).add(item);
        }

        final SortedSet<LinkableItem> collapsedItems = new ConcurrentSkipListSet<>();
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

    private CategoryKey createCategoryKeyFromLinkableItems(final SortedSet<LinkableItem> linkableItems) {
        final List<String> itemsToString = linkableItems
                                               .stream()
                                               .filter(LinkableItem::isSummarizable)
                                               .map(Objects::toString)
                                               .collect(Collectors.toList());
        return CategoryKey.from("summary", itemsToString);
    }

}
