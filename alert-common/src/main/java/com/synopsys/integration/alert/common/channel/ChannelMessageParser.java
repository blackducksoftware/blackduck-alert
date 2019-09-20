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
package com.synopsys.integration.alert.common.channel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public abstract class ChannelMessageParser {
    public String createMessage(MessageContentGroup messageContentGroup) {
        List<String> messagePieces = createMessagePieces(messageContentGroup);
        return String.join("", messagePieces);
    }

    public List<String> createMessagePieces(MessageContentGroup messageContentGroup) {
        LinkedList<String> messagePieces = new LinkedList<>();

        String commonTopicString = createLinkableItemString(messageContentGroup.getCommonTopic(), true);
        messagePieces.add(commonTopicString + getLineSeparator());

        for (ProviderMessageContent messageContent : messageContentGroup.getSubContent()) {
            messageContent.getSubTopic()
                .map(item -> createLinkableItemString(item, true) + getLineSeparator())
                .ifPresent(messagePieces::add);
            messagePieces.add(getTopicSectionSeparator() + getLineSeparator());

            SetMap<String, ComponentItem> componentItemSetMap = messageContent.groupRelatedComponentItems();
            for (Set<ComponentItem> similarItems : componentItemSetMap.values()) {
                List<String> componentItemMessagePieces = createComponentAndCategoryMessagePieces(similarItems);
                messagePieces.addAll(componentItemMessagePieces);
            }

            if (!messagePieces.isEmpty()) {
                String lastString = messagePieces.removeLast();
                String modifiedLastString = lastString + getLineSeparator();
                messagePieces.addLast(modifiedLastString);
            }
        }
        return messagePieces;
    }

    protected abstract String encodeString(String txt);

    protected abstract String emphasize(String txt);

    protected abstract String createLink(String txt, String url);

    protected abstract String getLineSeparator();

    protected String getTopicSectionSeparator() {
        return "- - - - - - - - - - - - - - - - - - - -";
    }

    protected List<String> createComponentAndCategoryMessagePieces(Set<ComponentItem> componentItems) {
        List<String> componentItemPieces = createCommonComponentMessagePieces(componentItems);
        List<String> categoryItemMessagePieces = createCategoryMessagePieces(componentItems);

        componentItemPieces.addAll(categoryItemMessagePieces);
        return componentItemPieces;
    }

    protected List<String> createCategoryMessagePieces(Set<ComponentItem> componentItems) {
        List<String> categoryItemMessagePieces = new LinkedList<>();
        boolean collapseOnCategory = componentItems.stream().allMatch(ComponentItem::collapseOnCategory);
        if (collapseOnCategory) {
            List<String> collapsedComponentPieces = createCollapsedCategoryItemPieces(componentItems);
            categoryItemMessagePieces.addAll(collapsedComponentPieces);
        } else {
            List<String> nonCollapsibleComponentPieces = createNonCollapsibleCategoryItemPieces(componentItems);
            categoryItemMessagePieces.addAll(nonCollapsibleComponentPieces);
        }
        return categoryItemMessagePieces;
    }

    protected String createLinkableItemString(LinkableItem linkableItem) {
        return createLinkableItemString(linkableItem, false);
    }

    protected String createLinkableItemString(LinkableItem linkableItem, boolean bold) {
        String name = encodeString(linkableItem.getName());
        String value = encodeString(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString;
        if (optionalUrl.isPresent()) {
            String linkableItemValueString = createLinkableItemValueString(linkableItem);
            formattedString = String.format("%s: %s", name, linkableItemValueString);
        } else {
            formattedString = String.format("%s: %s", name, value);
        }

        if (bold) {
            formattedString = emphasize(formattedString);
        }

        return formattedString;
    }

    protected List<String> createComponentAttributeMessagePieces(Collection<ComponentItem> componentItems) {
        SetMap<String, LinkableItem> attributesMap = componentItems
                                                         .stream()
                                                         .map(ComponentItem::getComponentAttributes)
                                                         .flatMap(Set::stream)
                                                         .collect(SetMap::createDefault, (map, item) -> map.add(item.getName(), item), SetMap::combine);
        List<String> attributeStrings = new LinkedList<>();
        for (Set<LinkableItem> similarAttributes : attributesMap.values()) {
            Optional<LinkableItem> optionalAttribute = getArbitraryElement(similarAttributes);
            if (optionalAttribute.isPresent()) {
                LinkableItem attribute = optionalAttribute.get();
                if (attribute.isCollapsible()) {
                    List<String> valuePieces = createLinkableItemValuesPieces(similarAttributes);
                    String valueString = String.join("", valuePieces);
                    String similarAttributesString = String.format("%s: %s", attribute.getName(), valueString);
                    attributeStrings.add(similarAttributesString);
                    attributeStrings.add(getLineSeparator());
                } else {
                    similarAttributes
                        .stream()
                        .map(this::createLinkableItemString)
                        .map(str -> str + getLineSeparator())
                        .forEach(attributeStrings::add);
                }
            }
        }
        attributeStrings.add(getLineSeparator());
        return attributeStrings;
    }

    private List<String> createCommonComponentMessagePieces(Collection<ComponentItem> componentItems) {
        Optional<ComponentItem> optionalArbitraryItem = getArbitraryElement(componentItems);
        List<String> commonComponentMessagePieces = new LinkedList<>();
        if (optionalArbitraryItem.isPresent()) {
            ComponentItem arbitraryItem = optionalArbitraryItem.get();
            StringBuilder componentItemBuilder = new StringBuilder()
                                                     .append("Category: ")
                                                     .append(arbitraryItem.getCategory())
                                                     .append(getLineSeparator())
                                                     .append("Operation: ")
                                                     .append(arbitraryItem.getOperation())
                                                     .append(getLineSeparator())
                                                     .append(createLinkableItemString(arbitraryItem.getComponent()))
                                                     .append(getLineSeparator());
            arbitraryItem
                .getSubComponent()
                .map(this::createLinkableItemString)
                .map(str -> str + getLineSeparator())
                .ifPresent(componentItemBuilder::append);

            commonComponentMessagePieces.add(componentItemBuilder.toString());
            commonComponentMessagePieces.addAll(createComponentAttributeMessagePieces(componentItems));
            commonComponentMessagePieces.add(getLineSeparator());
        }
        return commonComponentMessagePieces;
    }

    private List<String> createCollapsedCategoryItemPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        SetMap<String, ComponentItem> groupedItems = groupAndPrioritizeCollapsibleItems(componentItems);
        // TODO just get groupedItems.values()
        for (Map.Entry<String, Set<ComponentItem>> groupedItemsEntry : groupedItems.entrySet()) {
            Optional<ComponentItem> optionalGroupedItem = getArbitraryElement(groupedItemsEntry.getValue());
            if (optionalGroupedItem.isPresent()) {
                ComponentItem groupedItem = optionalGroupedItem.get();
                createCategoryGroupingString(groupedItem)
                    .map(str -> str + getLineSeparator())
                    .ifPresent(componentItemPieces::add);

                String categoryItemNameString = encodeString(groupedItem.getCategoryItem().getName() + ": ");
                componentItemPieces.add(categoryItemNameString);

                Set<LinkableItem> categoryItems = groupedItemsEntry.getValue()
                                                      .stream()
                                                      .map(ComponentItem::getCategoryItem)
                                                      .collect(Collectors.toSet());
                componentItemPieces.addAll(createLinkableItemValuesPieces(categoryItems));
                componentItemPieces.add(getLineSeparator() + getLineSeparator());
            }
        }
        return componentItemPieces;
    }

    private List<String> createNonCollapsibleCategoryItemPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        for (ComponentItem componentItem : componentItems) {
            createCategoryGroupingString(componentItem)
                .map(str -> str + getLineSeparator())
                .ifPresent(componentItemPieces::add);
            componentItemPieces.add(createLinkableItemString(componentItem.getCategoryItem()));
            componentItemPieces.add(getLineSeparator() + getLineSeparator());
        }
        return componentItemPieces;
    }

    private SetMap<String, ComponentItem> groupAndPrioritizeCollapsibleItems(Collection<ComponentItem> componentItems) {
        SetMap<String, ComponentItem> groupedAndPrioritizedItems = SetMap.createLinked();
        for (ComponentItem componentItem : componentItems) {
            String groupingString = componentItem.getCategoryGroupingAttribute()
                                        .map(item -> item.getName() + item.getValue())
                                        .orElse("DEFAULT_GROUPING_STRING");
            String priorityAndGroupingKey = componentItem.getPriority().name() + groupingString;
            Set<ComponentItem> updatedSet = groupedAndPrioritizedItems.add(priorityAndGroupingKey, componentItem);
            groupedAndPrioritizedItems.put(priorityAndGroupingKey, updatedSet);
        }
        return groupedAndPrioritizedItems;
    }

    private Optional<String> createCategoryGroupingString(ComponentItem componentItem) {
        return componentItem
                   .getCategoryGroupingAttribute()
                   .map(this::createLinkableItemString);
    }

    private List<String> createLinkableItemValuesPieces(Collection<LinkableItem> linkableItems) {
        if (1 == linkableItems.size()) {
            String singleValue = getArbitraryElement(linkableItems)
                                     .map(this::createLinkableItemValueString)
                                     .orElse("Unknown Value");
            return List.of(singleValue);
        }
        List<String> messagePieces = new LinkedList<>();
        for (LinkableItem item : linkableItems) {
            String linkableItemValueString = createLinkableItemValueString(item);
            messagePieces.add("[" + linkableItemValueString + "] ");
        }
        return messagePieces;
    }

    private String createLinkableItemValueString(LinkableItem linkableItem) {
        String value = encodeString(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString = value;
        if (optionalUrl.isPresent()) {
            String urlString = encodeString(optionalUrl.get());
            formattedString = createLink(value, urlString);
        }
        return formattedString;
    }

    private <T> Optional<T> getArbitraryElement(Collection<T> collection) {
        return collection
                   .stream()
                   .findAny();
    }

}
