package com.synopsys.integration.alert.common.workflow.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

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
            final CategoryKey uniqueKey = CategoryKey.from(UUID.randomUUID().toString());
            // Because this will not be a one-to-one mapping, we can't map it back to a notification.
            final Long invalidNotificationId = Long.MIN_VALUE;

            final SortedSet<LinkableItem> linkableItemsForOperation = getAllLinkableItemsFromCategoryItems(sortedEntry.getValue());
            final SortedSet<LinkableItem> summarizedLinkableItems = createSummarizedLinkableItems(sortedEntry.getValue(), linkableItemsForOperation);

            summarizedCategoryItems.add(new CategoryItem(uniqueKey, sortedEntry.getKey(), invalidNotificationId, summarizedLinkableItems));
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

    private SortedSet<LinkableItem> getAllLinkableItemsFromCategoryItems(final SortedSet<CategoryItem> categoryItems) {
        return categoryItems
                   .stream()
                   .flatMap(item -> item.getItems().stream())
                   .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
    }

    // TODO getting items of the same name may be the wrong approach (right now we are checking the item name again during count step)
    private SortedSet<LinkableItem> createSummarizedLinkableItems(final SortedSet<CategoryItem> categoryItems, final SortedSet<LinkableItem> linkableItems) {
        final Map<String, List<LinkableItem>> itemsOfSameName = new LinkedHashMap<>();
        categoryItems.forEach(item -> itemsOfSameName.putAll(item.getItemsOfSameName()));

        final SortedSet<LinkableItem> summarizedLinkableItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<String, List<LinkableItem>> similarItems : itemsOfSameName.entrySet()) {
            final Optional<LinkableItem> summarizableItem = similarItems.getValue()
                                                                .stream()
                                                                .findAny()
                                                                .filter(LinkableItem::isSummarizable);
            summarizableItem.ifPresent(item -> createNewItems(item, summarizedLinkableItems, similarItems.getKey(), linkableItems));
        }
        return summarizedLinkableItems;
    }

    private void createNewItems(final LinkableItem item, final SortedSet<LinkableItem> summarizedLinkableItems, final String oldItemName, final SortedSet<LinkableItem> linkableItems) {
        final boolean isCountable = item.isCountable();
        final boolean isNumericValue = item.isNumericValue();
        final String newItemName = generateSummaryItemName(oldItemName, isCountable, isNumericValue);

        if (isCountable) {
            final String newItemValue = generateCountAsString(oldItemName, linkableItems, isNumericValue);
            summarizedLinkableItems.add(new LinkableItem(newItemName, newItemValue));
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

    private String generateCountAsString(final String itemName, final SortedSet<LinkableItem> items, final boolean isNumericValue) {
        final int count = generateCount(itemName, items, isNumericValue);
        return Integer.toString(count);
    }

    private Integer generateCount(final String itemName, final SortedSet<LinkableItem> items, final boolean isNumericValue) {
        if (isNumericValue) {
            int count = 0;
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
        return items.size();
    }

    private SortedSet<LinkableItem> createLinkableItemsByValue(final String itemName, final SortedSet<LinkableItem> linkableItems) {
        final Map<String, Integer> valueCounts = countItemsOfSameValueForName(itemName, linkableItems);
        final SortedSet<LinkableItem> summarizedLinkableItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<String, Integer> valueCount : valueCounts.entrySet()) {
            // TODO original approach to tackle vulnerability counts:
            //            final String newName = String.format("%s - %s", itemName, valueCount.getKey());
            //            final String newValue = Integer.toString(valueCount.getValue());
            //  summarizedLinkableItems.add(new LinkableItem(newName, newValue));

            summarizedLinkableItems.add(new LinkableItem(itemName, valueCount.getKey()));
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

}
