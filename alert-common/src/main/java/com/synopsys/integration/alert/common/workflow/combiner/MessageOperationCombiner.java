/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.combiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageOperationCombiner extends AbstractMessageCombiner {
    private final Map<ItemOperation, BiConsumer<Map<String, ComponentItem>, ComponentItem>> operationFunctionMap;

    @Autowired
    public MessageOperationCombiner() {
        BiConsumer<Map<String, ComponentItem>, ComponentItem> addFunction = createAddFunction();
        BiConsumer<Map<String, ComponentItem>, ComponentItem> deleteFunction = createDeleteFunction();
        BiConsumer<Map<String, ComponentItem>, ComponentItem> infoFunction = createInfoFunction();
        operationFunctionMap = new EnumMap<>(ItemOperation.class);
        operationFunctionMap.put(ItemOperation.ADD, addFunction);
        operationFunctionMap.put(ItemOperation.UPDATE, addFunction);
        operationFunctionMap.put(ItemOperation.DELETE, deleteFunction);
        operationFunctionMap.put(ItemOperation.INFO, infoFunction);
    }

    @Override
    protected LinkedHashSet<ComponentItem> gatherComponentItems(Collection<ProviderMessageContent> groupedMessages) {
        List<ComponentItem> groupedComponentItems = new ArrayList<>();
        for (ProviderMessageContent messageContent : groupedMessages) {
            Map<String, ComponentItem> messageDataCache = new LinkedHashMap<>();
            messageContent.getComponentItems().forEach(item -> processOperation(messageDataCache, item));
            groupedComponentItems.addAll(messageDataCache.values());
        }
        Map<String, ComponentItem> groupDataCache = new LinkedHashMap<>();
        groupedComponentItems.forEach(item -> processOperation(groupDataCache, item));

        return combineComponentItems(new ArrayList<>(groupDataCache.values()));
    }

    private BiConsumer<Map<String, ComponentItem>, ComponentItem> createAddFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(false, true);
            categoryDataCache.put(key, componentItem);
        };
    }

    private BiConsumer<Map<String, ComponentItem>, ComponentItem> createDeleteFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(false, true);
            if (categoryDataCache.containsKey(key)) {
                categoryDataCache.remove(key);
            } else {
                categoryDataCache.put(key, componentItem);
            }
        };
    }

    private BiConsumer<Map<String, ComponentItem>, ComponentItem> createInfoFunction() {
        return (categoryDataCache, componentItem) -> {
            String key = componentItem.createKey(true, true);
            categoryDataCache.put(key, componentItem);
        };
    }

    private void processOperation(Map<String, ComponentItem> componentItemDataCache, ComponentItem item) {
        ItemOperation operation = item.getOperation();
        if (operationFunctionMap.containsKey(operation)) {
            BiConsumer<Map<String, ComponentItem>, ComponentItem> operationFunction = operationFunctionMap.get(operation);
            operationFunction.accept(componentItemDataCache, item);
        }
    }

}
