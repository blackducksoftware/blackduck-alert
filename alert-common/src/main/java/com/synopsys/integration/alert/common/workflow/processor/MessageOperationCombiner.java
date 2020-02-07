/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageOperationCombiner extends MessageCombiner {
    private final Map<ItemOperation, BiFunction<Map<String, ComponentItem>, ComponentItem, Void>> operationFunctionMap;

    @Autowired
    public MessageOperationCombiner() {
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> addFunction = createAddFunction();
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> deleteFunction = createDeleteFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
    }

    @Override
    public List<ProviderMessageContent> combine(List<ProviderMessageContent> messages) {
        final Map<ContentKey, List<ProviderMessageContent>> messagesGroupedByKey = new LinkedHashMap<>();
        for (ProviderMessageContent message : messages) {
            messagesGroupedByKey.computeIfAbsent(message.getContentKey(), k -> new LinkedList<>()).add(message);
        }

        List<ProviderMessageContent> combinedMessages = new ArrayList<>();
        for (final Map.Entry<ContentKey, List<ProviderMessageContent>> groupedMessageEntry : messagesGroupedByKey.entrySet()) {
            final List<ProviderMessageContent> groupedMessages = groupedMessageEntry.getValue();

            List<ComponentItem> groupCategoryItems = new ArrayList<>();
            for (ProviderMessageContent aggregateMessageContent : groupedMessageEntry.getValue()) {
                final Map<String, ComponentItem> messageDataCache = new LinkedHashMap<>();
                aggregateMessageContent.getComponentItems().forEach(item -> processOperation(messageDataCache, item));
                groupCategoryItems.addAll(messageDataCache.values());
            }
            Map<String, ComponentItem> groupDataCache = new LinkedHashMap<>();
            groupCategoryItems.forEach(item -> processOperation(groupDataCache, item));

            final LinkedHashSet<ComponentItem> combinedComponentItems = combineComponentItems(new ArrayList<>(groupDataCache.values()));

            final Optional<ProviderMessageContent> arbitraryMessage = groupedMessages
                                                                          .stream()
                                                                          .findAny();

            if (arbitraryMessage.isPresent()) {
                try {
                    final ProviderMessageContent newMessage = createNewMessage(arbitraryMessage.get(), combinedComponentItems);
                    combinedMessages.add(newMessage);
                } catch (AlertException e) {
                    // If this happens, it means there is a bug in the Collector logic.
                    throw new AlertRuntimeException(e);
                }
            }
        }
        return combinedMessages;
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createAddFunction() {
        return (categoryDataCache, categoryItem) -> {
            categoryDataCache.put(categoryItem.getComponentKeys().getDeepKey(), categoryItem);
            return null;
        };
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createDeleteFunction() {
        return (categoryDataCache, categoryItem) -> {
            final String key = categoryItem.getComponentKeys().getDeepKey();
            if (categoryDataCache.containsKey(key)) {
                categoryDataCache.remove(key);
            } else {
                categoryDataCache.put(key, categoryItem);
            }
            return null;
        };
    }

    private void processOperation(final Map<String, ComponentItem> categoryDataCache, final ComponentItem item) {
        final ItemOperation operation = item.getOperation();
        if (operationFunctionMap.containsKey(operation)) {
            final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> operationFunction = operationFunctionMap.get(operation);
            operationFunction.apply(categoryDataCache, item);
        }
    }

    private Optional<ProviderMessageContent> rebuildTopic(final ProviderMessageContent currentContent, final Collection<ComponentItem> componentItems) {
        if (!componentItems.isEmpty()) {
            final LinkableItem topic = currentContent.getTopic();
            final LinkableItem provider = currentContent.getProvider();
            final Optional<LinkableItem> optionalSubTopic = currentContent.getSubTopic();
            final ProviderMessageContent.Builder messageBuilder = new ProviderMessageContent.Builder();

            final String url = topic.getUrl().orElse(null);
            messageBuilder.applyProvider(provider.getValue(), provider.getUrl().orElse(null));
            messageBuilder.applyTopic(topic.getName(), topic.getValue(), url);

            if (optionalSubTopic.isPresent()) {
                final LinkableItem subTopic = optionalSubTopic.get();
                messageBuilder.applySubTopic(subTopic.getName(), subTopic.getValue(), subTopic.getUrl().orElse(null));
            }

            messageBuilder.applyAllComponentItems(componentItems);

            final ProviderMessageContent newProviderMessageContent;
            try {
                newProviderMessageContent = messageBuilder.build();
                return Optional.of(newProviderMessageContent);
            } catch (AlertException e) {
            }
        }
        return Optional.empty();
    }

}
