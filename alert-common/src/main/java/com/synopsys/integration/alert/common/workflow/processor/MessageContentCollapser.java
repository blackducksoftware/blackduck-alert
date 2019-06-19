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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageContentCollapser {
    private final Map<ItemOperation, BiFunction<Map<String, ComponentItem>, ComponentItem, Void>> operationFunctionMap;

    @Autowired
    public MessageContentCollapser() {
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> addFunction = createAddFunction();
        final BiFunction<Map<String, ComponentItem>, ComponentItem, Void> deleteFunction = createDeleteFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
    }

    public List<ProviderMessageContent> collapse(final List<ProviderMessageContent> messages) {
        final List<ProviderMessageContent> collapsedMessages = new ArrayList<>(messages.size());
        for (final ProviderMessageContent message : messages) {
            final Map<String, ComponentItem> categoryDataCache = new LinkedHashMap<>();
            message.getComponentItems().forEach(item -> processOperation(categoryDataCache, item));

            final Optional<ProviderMessageContent> collapsedContent = rebuildTopic(message, categoryDataCache.values());
            collapsedContent.ifPresent(collapsedMessages::add);
        }

        return collapsedMessages;
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createAddFunction() {
        return (categoryDataCache, categoryItem) -> {
            categoryDataCache.put(categoryItem.getComponentKey().getFullKey(), categoryItem);
            return null;
        };
    }

    private BiFunction<Map<String, ComponentItem>, ComponentItem, Void> createDeleteFunction() {
        return (categoryDataCache, categoryItem) -> {
            final String key = categoryItem.getComponentKey().getFullKey();
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
