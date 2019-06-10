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

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;

@Component
public class DefaultMessageContentProcessor extends MessageContentProcessor {

    @Autowired
    public DefaultMessageContentProcessor() {
        super(FormatType.DEFAULT);
    }

    @Override
    public List<MessageContentGroup> process(final List<AggregateMessageContent> messages) {
        final Map<DefaultMessageContentProcessor.TopicAndSubTopicPair, List<AggregateMessageContent>> messagesGroupedByTopicAndSubTopic = groupByTopicAndSubTopicValue(messages);

        final Map<String, MessageContentGroup> messageGroups = new LinkedHashMap<>();
        for (final Map.Entry<DefaultMessageContentProcessor.TopicAndSubTopicPair, List<AggregateMessageContent>> groupedMessageEntry : messagesGroupedByTopicAndSubTopic.entrySet()) {
            final DefaultMessageContentProcessor.TopicAndSubTopicPair topicAndSubTopic = groupedMessageEntry.getKey();
            final LinkableItem topic = topicAndSubTopic.getLeft();
            final SortedSet<CategoryItem> combinedCategoryItems = gatherCategoryItems(groupedMessageEntry.getValue());

            final AggregateMessageContent newMessage = new AggregateMessageContent(topic.getName(), topic.getValue(), topic.getUrl().orElse(null), topicAndSubTopic.getRight(), combinedCategoryItems);
            messageGroups.computeIfAbsent(topic.getValue(), ignored -> new MessageContentGroup()).add(newMessage);
        }

        return new ArrayList<>(messageGroups.values());
    }

    private Map<DefaultMessageContentProcessor.TopicAndSubTopicPair, List<AggregateMessageContent>> groupByTopicAndSubTopicValue(final List<AggregateMessageContent> messages) {
        final Map<DefaultMessageContentProcessor.TopicAndSubTopicPair, List<AggregateMessageContent>> groupedMessages = new LinkedHashMap<>();
        for (final AggregateMessageContent message : messages) {
            final LinkableItem topic = new LinkableItem(message.getName(), message.getValue(), message.getUrl().orElse(null));
            final LinkableItem subTopic = message.getSubTopic().orElse(null);
            final DefaultMessageContentProcessor.TopicAndSubTopicPair topicAndSubTopic = new DefaultMessageContentProcessor.TopicAndSubTopicPair(topic, subTopic);

            groupedMessages.computeIfAbsent(topicAndSubTopic, ignored -> new ArrayList<>()).add(message);
        }
        return groupedMessages;
    }

    private SortedSet<CategoryItem> gatherCategoryItems(final List<AggregateMessageContent> groupedMessages) {
        final List<CategoryItem> allCategoryItems = groupedMessages
                                                        .stream()
                                                        .map(AggregateMessageContent::getCategoryItems)
                                                        .flatMap(SortedSet::stream)
                                                        .collect(Collectors.toList());
        return combineCategoryItems(allCategoryItems);
    }

    private SortedSet<CategoryItem> combineCategoryItems(final List<CategoryItem> allCategoryItems) {
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
            newCategoryItem.setComparator(categoryItem.createComparator());
            keyToItems.put(categoryKey, newCategoryItem);
        }
        return new TreeSet<>(keyToItems.values());
    }

    private CategoryKey generateCategoryKey(final CategoryItem categoryItem) {
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

    private SortedSet<LinkableItem> combineLinkableItems(final SortedSet<LinkableItem> oldItems, final SortedSet<LinkableItem> newItems) {
        final SortedSet<LinkableItem> combinedItems = new TreeSet<>(oldItems);
        newItems
            .stream()
            .filter(LinkableItem::isCollapsible)
            .forEach(combinedItems::add);
        return combinedItems;
    }

    private class TopicAndSubTopicPair extends Pair<LinkableItem, LinkableItem> {
        private final LinkableItem topic;
        private final LinkableItem subTopicNullable;

        public TopicAndSubTopicPair(final LinkableItem topic, final LinkableItem subTopicNullable) {
            this.topic = topic;
            this.subTopicNullable = subTopicNullable;
        }

        @Override
        public LinkableItem getLeft() {
            return topic;
        }

        @Override
        public LinkableItem getRight() {
            return subTopicNullable;
        }

        @Override
        public LinkableItem setValue(final LinkableItem subTopic) {
            throw new UnsupportedOperationException();
        }

    }

}
