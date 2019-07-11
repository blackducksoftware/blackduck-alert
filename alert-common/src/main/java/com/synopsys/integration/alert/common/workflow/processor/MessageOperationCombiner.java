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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.workflow.processor.model.TopicAndSubTopicPair;

@Component
public class MessageOperationCombiner extends MessageCombiner {
    private final Map<ItemOperation, BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void>> operationFunctionMap;

    @Autowired
    public MessageOperationCombiner() {
        final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> addFunction = createAddFunction();
        final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> deleteFunction = createDeleteFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
    }

    @Override
    public List<AggregateMessageContent> combine(List<AggregateMessageContent> messages) {
        final Map<TopicAndSubTopicPair, List<AggregateMessageContent>> messagesGroupedByTopicAndSubTopic = groupByTopicAndSubTopicValue(messages);

        List<AggregateMessageContent> combinedMessages = new ArrayList<>();
        for (final Map.Entry<TopicAndSubTopicPair, List<AggregateMessageContent>> groupedMessageEntry : messagesGroupedByTopicAndSubTopic.entrySet()) {
            final TopicAndSubTopicPair topicAndSubTopic = groupedMessageEntry.getKey();
            final LinkableItem topic = topicAndSubTopic.getLeft();

            List<CategoryItem> groupCategoryItems = new ArrayList<>();
            for (AggregateMessageContent aggregateMessageContent : groupedMessageEntry.getValue()) {
                final Map<CategoryKey, CategoryItem> messageDataCache = new LinkedHashMap<>();
                aggregateMessageContent.getCategoryItems().forEach(item -> processOperation(messageDataCache, item));
                groupCategoryItems.addAll(messageDataCache.values());
            }
            Map<CategoryKey, CategoryItem> groupDataCache = new LinkedHashMap<>();
            groupCategoryItems.forEach(item -> processOperation(groupDataCache, item));

            final SortedSet<CategoryItem> combinedCategoryItems = combineCategoryItems(new ArrayList<>(groupDataCache.values()));

            final AggregateMessageContent newMessage = new AggregateMessageContent(topic.getName(), topic.getValue(), topic.getUrl().orElse(null), topicAndSubTopic.getRight(), new HashSet<>(combinedCategoryItems), Date.from(Instant.now()));
            combinedMessages.add(newMessage);
        }
        return combinedMessages;
    }

    private BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> createAddFunction() {
        return (messageDataCache, categoryItem) -> {
            messageDataCache.put(categoryItem.getCategoryKey(), categoryItem);
            return null;
        };
    }

    private BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> createDeleteFunction() {
        return (messageDataCache, categoryItem) -> {
            final CategoryKey key = categoryItem.getCategoryKey();
            if (messageDataCache.containsKey(key)) {
                messageDataCache.remove(key);
            } else {
                messageDataCache.put(key, categoryItem);
            }
            return null;
        };
    }

    private void processOperation(final Map<CategoryKey, CategoryItem> messageDataCache, final CategoryItem item) {
        final ItemOperation operation = item.getOperation();
        if (operationFunctionMap.containsKey(operation)) {
            final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> operationFunction = operationFunctionMap.get(operation);
            operationFunction.apply(messageDataCache, item);
        }
    }

}
