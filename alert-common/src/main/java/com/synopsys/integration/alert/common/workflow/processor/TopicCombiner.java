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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.workflow.processor.model.TopicAndSubTopicPair;

@Component
public class TopicCombiner {

    public List<AggregateMessageContent> process(List<AggregateMessageContent> messages) {
        final Map<TopicAndSubTopicPair, List<AggregateMessageContent>> messagesGroupedByTopicAndSubTopic = groupByTopicAndSubTopicValue(messages);

        List<AggregateMessageContent> combinedMessages = new ArrayList<>();
        for (final Map.Entry<TopicAndSubTopicPair, List<AggregateMessageContent>> groupedMessageEntry : messagesGroupedByTopicAndSubTopic.entrySet()) {
            final TopicAndSubTopicPair topicAndSubTopic = groupedMessageEntry.getKey();
            final LinkableItem topic = topicAndSubTopic.getLeft();
            final SortedSet<CategoryItem> combinedCategoryItems = gatherCategoryItems(groupedMessageEntry.getValue());

            final AggregateMessageContent newMessage = new AggregateMessageContent(topic.getName(), topic.getValue(), topic.getUrl().orElse(null), topicAndSubTopic.getRight(), combinedCategoryItems);
            combinedMessages.add(newMessage);
        }
        return combinedMessages;
    }

    protected Map<TopicAndSubTopicPair, List<AggregateMessageContent>> groupByTopicAndSubTopicValue(final List<AggregateMessageContent> messages) {
        final Map<TopicAndSubTopicPair, List<AggregateMessageContent>> groupedMessages = new LinkedHashMap<>();
        for (final AggregateMessageContent message : messages) {
            final LinkableItem topic = new LinkableItem(message.getName(), message.getValue(), message.getUrl().orElse(null));
            final LinkableItem subTopic = message.getSubTopic().orElse(null);
            final TopicAndSubTopicPair topicAndSubTopic = new TopicAndSubTopicPair(topic, subTopic);

            groupedMessages.computeIfAbsent(topicAndSubTopic, ignored -> new ArrayList<>()).add(message);
        }
        return groupedMessages;
    }

    protected SortedSet<CategoryItem> gatherCategoryItems(final List<AggregateMessageContent> groupedMessages) {
        final List<CategoryItem> allCategoryItems = groupedMessages
                                                        .stream()
                                                        .map(AggregateMessageContent::getCategoryItems)
                                                        .flatMap(SortedSet::stream)
                                                        .collect(Collectors.toList());
        return combineCategoryItems(allCategoryItems);
    }

    protected SortedSet<CategoryItem> combineCategoryItems(final List<CategoryItem> allCategoryItems) {
        // The amount of collapsing we do makes this impossible to map back to a single notification.
        final Map<CategoryKey, CategoryItem> keyToItems = new LinkedHashMap<>();
        for (final CategoryItem categoryItem : allCategoryItems) {
            final CategoryKey categoryKey = generateCategoryKey(categoryItem);
            final CategoryItem oldItem = keyToItems.get(categoryKey);

            // Always use the newest notification because the audit entry will appear first.
            final Long notificationId = categoryItem.getNotificationId();
            final SortedSet<LinkableItem> linkableItems;
            if (null != oldItem) {
                linkableItems = combineLinkableItems(oldItem.getItems(), categoryItem.getItems());
            } else {
                linkableItems = categoryItem.getItems();
            }

            final CategoryItem newCategoryItem = new CategoryItem(categoryKey, categoryItem.getOperation(), notificationId, linkableItems);
            keyToItems.put(categoryKey, newCategoryItem);
        }
        return new TreeSet<>(keyToItems.values());
    }

    protected CategoryKey generateCategoryKey(final CategoryItem categoryItem) {
        final List<String> keyParts = new ArrayList<>();
        keyParts.add(categoryItem.getOperation().name());
        for (final LinkableItem item : categoryItem.getItems()) {
            if (!item.isCollapsible()) {
                keyParts.add(item.getName());
                keyParts.add(item.getValue());
            }
        }
        return CategoryKey.from("ignored", keyParts);
    }

    protected SortedSet<LinkableItem> combineLinkableItems(final SortedSet<LinkableItem> oldItems, final SortedSet<LinkableItem> newItems) {
        final SortedSet<LinkableItem> combinedItems = new TreeSet<>(oldItems);
        newItems
            .stream()
            .filter(LinkableItem::isCollapsible)
            .forEach(combinedItems::add);
        return combinedItems;
    }
}
