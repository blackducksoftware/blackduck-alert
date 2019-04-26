package com.synopsys.integration.alert.common.workflow.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

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
            // Since this will not be a one-to-one mapping, we can't map it back to a notification.
            final Long invalidNotificationId = Long.MIN_VALUE;
            final SortedSet<LinkableItem> linkableItems = createSummarizedLinkableItems(sortedEntry.getValue());

            summarizedCategoryItems.add(new CategoryItem(uniqueKey, sortedEntry.getKey(), invalidNotificationId, linkableItems));
        }

        return new AggregateMessageContent(message.getName(), message.getValue(), message.getUrl().orElse(null), message.getSubTopic().orElse(null), summarizedCategoryItems);
    }

    private Map<ItemOperation, SortedSet<CategoryItem>> sortByOperation(final SortedSet<CategoryItem> originalCategoryItems) {
        final Map<ItemOperation, SortedSet<CategoryItem>> itemsByOperation = new LinkedHashMap<>();
        for (final CategoryItem categoryItem : originalCategoryItems) {
            itemsByOperation.putIfAbsent(categoryItem.getOperation(), new ConcurrentSkipListSet<>()).add(categoryItem);
        }
        return itemsByOperation;
    }

    private SortedSet<LinkableItem> createSummarizedLinkableItems(final SortedSet<CategoryItem> categoryItems) {
        final Map<String, List<LinkableItem>> itemsOfSameName = new LinkedHashMap<>();
        categoryItems.forEach(item -> itemsOfSameName.putAll(item.getItemsOfSameName()));

        final SortedSet<LinkableItem> linkableItems = new ConcurrentSkipListSet<>();
        for (final Map.Entry<String, List<LinkableItem>> similarItems : itemsOfSameName.entrySet()) {
            final List<LinkableItem> itemsForCategory = similarItems.getValue();
            if (doThatMatter(itemsForCategory)) {
                final String newItemName = generateSummaryItemName(similarItems.getKey());
                final String countString = generateCountAsString(itemsForCategory);

                linkableItems.add(new LinkableItem(newItemName, countString));
            }
        }
        return linkableItems;
    }

    // TODO rename this method
    private boolean doThatMatter(final List<LinkableItem> items) {
        return items
                   .stream()
                   .findAny()
                   .filter(LinkableItem::isSummarizable)
                   .isPresent();
    }

    private String generateSummaryItemName(final String oldItemName) {
        return String.format("%s Change Count", oldItemName);
    }

    private String generateCountAsString(final List<LinkableItem> items) {
        if (items.isEmpty()) {
            return "0";
        }

        final int numberOfItems = items.size();
        return Integer.toString(numberOfItems);
    }

}
