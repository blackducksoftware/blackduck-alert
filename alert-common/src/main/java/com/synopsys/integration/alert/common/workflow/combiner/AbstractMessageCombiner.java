/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.combiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.datastructure.SetMap;

public abstract class AbstractMessageCombiner implements MessageCombiner {
    @Override
    public final List<ProviderMessageContent> combine(List<ProviderMessageContent> messages) {
        SetMap<ContentKey, ProviderMessageContent> messagesGroupedByKey = SetMap.createLinked();
        messages.forEach(message -> messagesGroupedByKey.add(message.getContentKey(), message));

        List<ProviderMessageContent> combinedMessages = new ArrayList<>();
        for (Set<ProviderMessageContent> groupedMessages : messagesGroupedByKey.values()) {
            combineGroupedMessages(groupedMessages)
                .ifPresent(combinedMessages::add);
        }
        return combinedMessages;
    }

    public ProviderMessageContent createNewMessage(ProviderMessageContent oldMessage, Collection<ComponentItem> componentItems) throws AlertException {
        LinkableItem provider = oldMessage.getProvider();
        LinkableItem topic = oldMessage.getTopic();
        Optional<LinkableItem> optionalSubTopic = oldMessage.getSubTopic();
        String subTopicName = optionalSubTopic.map(LinkableItem::getName).orElse(null);
        String subTopicValue = optionalSubTopic.map(LinkableItem::getValue).orElse(null);
        String subTopicUrl = optionalSubTopic.flatMap(LinkableItem::getUrl).orElse(null);
        ItemOperation action = oldMessage.getAction().orElse(null);
        Long notificationId = oldMessage.getNotificationId().orElse(null);

        return new ProviderMessageContent.Builder()
                   .applyProvider(provider.getName(), oldMessage.getProviderConfigId(), provider.getValue(), provider.getUrl().orElse(null))
                   .applyTopic(topic.getName(), topic.getValue(), topic.getUrl().orElse(null))
                   .applySubTopic(subTopicName, subTopicValue, subTopicUrl)
                   .applyAction(action)
                   .applyNotificationId(notificationId)
                   .applyAllComponentItems(componentItems)
                   .build();
    }

    protected abstract LinkedHashSet<ComponentItem> gatherComponentItems(Collection<ProviderMessageContent> groupedMessages);

    protected LinkedHashSet<ComponentItem> combineComponentItems(List<ComponentItem> allComponentItems) {
        // The amount of collapsing we do makes this impossible to map back to a single notification.
        Map<String, ComponentItem> keyToItems = new LinkedHashMap<>();
        for (ComponentItem componentItem : allComponentItems) {
            Set<LinkableItem> componentAttributes = componentItem.getComponentAttributes();
            String key = componentItem.createKey(true, true);
            ComponentItem oldItem = keyToItems.get(key);

            Set<LinkableItem> combinedAttributes = new LinkedHashSet<>();
            if (null != oldItem) {
                combinedAttributes.addAll(oldItem.getComponentAttributes());
            }
            combinedAttributes.addAll(componentAttributes);

            try {
                ComponentItem newComponentItem = createNewComponentItem(componentItem, combinedAttributes);
                keyToItems.put(key, newComponentItem);
            } catch (AlertException e) {
                // If this happens, it means there is a bug in the Collector logic.
                throw new AlertRuntimeException(e);
            }
        }
        return sortComponentItems(keyToItems.values());
    }

    private Optional<ProviderMessageContent> combineGroupedMessages(Collection<ProviderMessageContent> groupedMessages) {
        LinkedHashSet<ComponentItem> combinedComponentItems = gatherComponentItems(groupedMessages);

        Optional<ProviderMessageContent> arbitraryMessage = groupedMessages
                                                                .stream()
                                                                .findAny();
        if (arbitraryMessage.isPresent()) {
            try {
                ProviderMessageContent newMessage = createNewMessage(arbitraryMessage.get(), combinedComponentItems);
                return Optional.of(newMessage);
            } catch (AlertException e) {
                // If this happens, it means there is a bug in the Collector logic.
                throw new AlertRuntimeException(e);
            }
        }
        return Optional.empty();
    }

    private ComponentItem createNewComponentItem(ComponentItem oldItem, Collection<LinkableItem> componentAttributes) throws AlertException {
        return new ComponentItem.Builder()
                   .applyCategory(oldItem.getCategory())
                   .applyOperation(oldItem.getOperation())
                   .applyPriority(oldItem.getPriority())
                   .applyComponentData(oldItem.getComponent())
                   .applySubComponent(oldItem.getSubComponent().orElse(null))
                   .applyComponentItemCallbackInfo(oldItem.getCallbackInfo().orElse(null))
                   .applyCategoryItem(oldItem.getCategoryItem())
                   .applyCategoryGroupingAttribute(oldItem.getCategoryGroupingAttribute().orElse(null))
                   .applyCollapseOnCategory(oldItem.collapseOnCategory())
                   .applyAllComponentAttributes(componentAttributes)
                   .applyNotificationIds(oldItem.getNotificationIds())
                   .build();
    }

    private LinkedHashSet<ComponentItem> sortComponentItems(Collection<ComponentItem> componentItems) {
        return componentItems
                   .stream()
                   .sorted(ComponentItem.createDefaultComparator())
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
