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
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class DigestMessageContentProcessor extends MessageContentProcessor {
    private final Map<ItemOperation, BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void>> operationFunctionMap;

    @Autowired
    public DigestMessageContentProcessor() {
        super(FormatType.DIGEST);
        final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> addFunction = createAddFunction();
        final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> deleteFunction = createDeleteFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
    }

    @Override
    public List<AggregateMessageContent> process(final List<AggregateMessageContent> messages) {
        final List<AggregateMessageContent> collapsedTopicList = new ArrayList<>(messages.size());
        for (final AggregateMessageContent topic : messages) {
            final Map<CategoryKey, CategoryItem> categoryDataCache = new LinkedHashMap<>();
            topic.getCategoryItems().forEach(item -> processOperation(categoryDataCache, item));

            final Optional<AggregateMessageContent> collapsedContent = rebuildTopic(topic, categoryDataCache.values());
            if (collapsedContent.isPresent()) {
                collapsedTopicList.add(collapsedContent.get());
            }
        }

        return collapsedTopicList;
    }

    private BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> createAddFunction() {
        return (categoryDataCache, categoryItem) -> {
            categoryDataCache.put(categoryItem.getCategoryKey(), categoryItem);
            return null;
        };
    }

    private BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> createDeleteFunction() {
        return (categoryDataCache, categoryItem) -> {
            final CategoryKey key = categoryItem.getCategoryKey();
            if (categoryDataCache.containsKey(key)) {
                categoryDataCache.remove(key);
            } else {
                categoryDataCache.put(key, categoryItem);
            }
            return null;
        };
    }

    private void processOperation(final Map<CategoryKey, CategoryItem> categoryDataCache, final CategoryItem item) {
        final ItemOperation operation = item.getOperation();
        if (operationFunctionMap.containsKey(operation)) {
            final BiFunction<Map<CategoryKey, CategoryItem>, CategoryItem, Void> operationFunction = operationFunctionMap.get(operation);
            operationFunction.apply(categoryDataCache, item);
        }
    }

    private Optional<AggregateMessageContent> rebuildTopic(final AggregateMessageContent currentContent, final Collection<CategoryItem> categoryItemCollection) {
        if (categoryItemCollection.isEmpty()) {
            return Optional.empty();
        } else {
            final String url = currentContent.getUrl().orElse(null);
            final LinkableItem subTopic = currentContent.getSubTopic().orElse(null);
            return Optional.of(new AggregateMessageContent(currentContent.getName(), currentContent.getValue(), url, subTopic, new TreeSet<>(categoryItemCollection)));
        }
    }

}
