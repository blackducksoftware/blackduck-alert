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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentKeys;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageCombiner {

    public List<ProviderMessageContent> combine(List<ProviderMessageContent> messages) {
        final Map<ContentKey, List<ProviderMessageContent>> messagesGroupedByKey = new LinkedHashMap<>();
        for (ProviderMessageContent message : messages) {
            messagesGroupedByKey.computeIfAbsent(message.getContentKey(), k -> new LinkedList<>()).add(message);
        }

        List<ProviderMessageContent> combinedMessages = new ArrayList<>();
        for (final Map.Entry<ContentKey, List<ProviderMessageContent>> groupedMessageEntry : messagesGroupedByKey.entrySet()) {
            final List<ProviderMessageContent> groupedMessages = groupedMessageEntry.getValue();
            final LinkedHashSet<ComponentItem> combinedComponentItems = gatherComponentItems(groupedMessages);

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

    protected LinkedHashSet<ComponentItem> gatherComponentItems(final List<ProviderMessageContent> groupedMessages) {
        final List<ComponentItem> allComponentItems = groupedMessages
                                                          .stream()
                                                          .map(ProviderMessageContent::getComponentItems)
                                                          .flatMap(Set::stream)
                                                          .collect(Collectors.toList());
        return combineComponentItems(allComponentItems);
    }

    protected LinkedHashSet<ComponentItem> combineComponentItems(final List<ComponentItem> allComponentItems) {
        // The amount of collapsing we do makes this impossible to map back to a single notification.
        final Map<String, ComponentItem> keyToItems = new LinkedHashMap<>();
        for (final ComponentItem componentItem : allComponentItems) {
            final Set<LinkableItem> componentAttributes = componentItem.getComponentAttributes();
            final String key = generateKey(componentItem.getComponentKeys(), componentAttributes, componentItem.getOperation());
            final ComponentItem oldItem = keyToItems.get(key);

            final Set<LinkableItem> linkableItems = new LinkedHashSet<>();
            if (null != oldItem) {
                linkableItems.addAll(oldItem.getComponentAttributes());
                linkableItems.addAll(componentAttributes);
            } else {
                linkableItems.addAll(componentAttributes);
            }

            try {
                ComponentItem newComponentItem = createNewComponentItem(componentItem, linkableItems);
                keyToItems.put(key, newComponentItem);
            } catch (AlertException e) {
                // If this happens, it means there is a bug in the Collector logic.
                throw new AlertRuntimeException(e);
            }
        }
        return sortComponentItems(keyToItems.values());
    }

    public ProviderMessageContent createNewMessage(ProviderMessageContent oldMessage, Collection<ComponentItem> componentItems) throws AlertException {
        LinkableItem provider = oldMessage.getProvider();
        LinkableItem topic = oldMessage.getTopic();
        Optional<LinkableItem> optionalSubTopic = oldMessage.getSubTopic();
        String subTopicName = optionalSubTopic.map(LinkableItem::getName).orElse(null);
        String subTopicValue = optionalSubTopic.map(LinkableItem::getValue).orElse(null);
        String subTopicUrl = optionalSubTopic.flatMap(LinkableItem::getUrl).orElse(null);

        return new ProviderMessageContent.Builder()
                   .applyProvider(provider.getValue(), provider.getUrl().orElse(null))
                   .applyTopic(topic.getName(), topic.getValue(), topic.getUrl().orElse(null))
                   .applySubTopic(subTopicName, subTopicValue, subTopicUrl)
                   .applyAllComponentItems(componentItems)
                   .build();
    }

    public ComponentItem createNewComponentItem(ComponentItem oldItem, Collection<LinkableItem> componentAttributes) throws AlertException {
        LinkableItem component = oldItem.getComponent();
        LinkableItem nullableSubComponent = oldItem.getSubComponent().orElse(null);
        return new ComponentItem.Builder()
                   .applyCategory(oldItem.getCategory())
                   .applyPriority(oldItem.getPriority())
                   .applyComponentData(component.getName(), component.getValue(), component.getUrl().orElse(null))
                   .applySubComponent(nullableSubComponent)
                   .applyOperation(oldItem.getOperation())
                   .applyNotificationIds(oldItem.getNotificationIds())
                   .applyAllComponentAttributes(componentAttributes)
                   .build();
    }

    private String generateKey(ComponentKeys componentKey, Collection<LinkableItem> componentAttributes, ItemOperation operation) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(componentKey.getShallowKey());
        componentAttributes
            .stream()
            .filter(item -> !item.isCollapsible())
            .forEach(item -> {
                keyBuilder.append(item.getName());
                keyBuilder.append(item.getValue());
                item.getUrl().ifPresent(keyBuilder::append);
            });
        keyBuilder.append(operation.toString());
        return keyBuilder.toString();
    }

    private LinkedHashSet<ComponentItem> sortComponentItems(Collection<ComponentItem> componentItems) {
        return componentItems
                   .stream()
                   .sorted(ComponentItem.createDefaultComparator())
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
