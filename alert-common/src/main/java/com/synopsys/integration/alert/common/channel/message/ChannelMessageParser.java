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
package com.synopsys.integration.alert.common.channel.message;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
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
        String header = createHeader(messageContentGroup);
        if (StringUtils.isNotBlank(header)) {
            messagePieces.add(header);
        }
        String commonTopicString = getCommonTopic(messageContentGroup);
        messagePieces.add(commonTopicString);

        for (ProviderMessageContent messageContent : messageContentGroup.getSubContent()) {
            String componentSubTopic = getComponentSubTopic(messageContent);
            if (StringUtils.isNotBlank(componentSubTopic)) {
                messagePieces.add(componentSubTopic);
            }

            String componentItems = createComponentItemMessage(messageContent);
            if (StringUtils.isNotBlank(componentItems)) {
                messagePieces.add(componentItems);
            }
        }

        String footer = createFooter(messageContentGroup);
        if (StringUtils.isNotBlank(footer)) {
            messagePieces.add(footer);
        }
        return messagePieces;
    }

    public String createHeader(MessageContentGroup messageContentGroup) {
        String messageHeader = String.format("Begin %s Content", messageContentGroup.getCommonProvider().getValue());
        String headerSeparator = createMessageSeparator(messageHeader);
        return headerSeparator + getLineSeparator();
    }

    public String getCommonTopic(MessageContentGroup messageContentGroup) {
        return createLinkableItemString(messageContentGroup.getCommonTopic(), true) + getLineSeparator();
    }

    public String getComponentSubTopic(ProviderMessageContent messageContent) {
        return messageContent.getSubTopic()
                   .map(item -> createLinkableItemString(item, true) + getLineSeparator())
                   .orElse("");
    }

    public String createComponentItemMessage(ProviderMessageContent messageContent) {
        if (messageContent.isTopLevelActionOnly()) {
            return messageContent
                       .getAction()
                       .map(ItemOperation::name)
                       .map(action -> String.format("%s Action: %s%s", messageContent.getTopic().getName(), action, getLineSeparator()))
                       .orElse("");
        } else {
            List<String> messagePieces = new LinkedList<>();
            SetMap<String, ComponentItem> componentItemSetMap = messageContent.groupRelatedComponentItems();
            for (Set<ComponentItem> similarItems : componentItemSetMap.values()) {
                messagePieces.add(getSectionSeparator() + getLineSeparator());
                List<String> componentItemMessagePieces = createComponentAndCategoryMessagePieces(similarItems);
                messagePieces.addAll(componentItemMessagePieces);
            }
            messagePieces.add(getLineSeparator());
            return String.join("", messagePieces);
        }
    }

    public String createFooter(MessageContentGroup messageContentGroup) {
        String footerSeparator = createMessageSeparator("End Content");
        return footerSeparator + getLineSeparator();
    }

    protected abstract String encodeString(String txt);

    protected abstract String emphasize(String txt);

    protected abstract String createLink(String txt, String url);

    protected abstract String getLineSeparator();

    protected String getListItemPrefix() {
        return "- ";
    }

    protected String getSectionSeparator() {
        return "- - - - - - - - - - - - - - - - - - - -";
    }

    protected String createMessageSeparator(String title) {
        return getSectionSeparator() + " " + title + " " + getSectionSeparator();
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
                                                         .flatMap(LinkedHashSet::stream)
                                                         .collect(SetMap::createLinked, (map, item) -> map.add(item.getName(), item), SetMap::combine);
        List<String> attributeStrings = new LinkedList<>();
        for (Set<LinkableItem> similarAttributes : attributesMap.values()) {
            Optional<LinkableItem> optionalAttribute = getArbitraryElement(similarAttributes);
            if (optionalAttribute.isPresent()) {
                LinkableItem attribute = optionalAttribute.get();
                if (attribute.isCollapsible()) {
                    List<String> valuePieces = createLinkableItemValuesPieces(similarAttributes);
                    String valueString = String.join("", valuePieces);
                    String similarAttributesString = String.format("%s%s: %s", getListItemPrefix(), attribute.getName(), valueString);
                    attributeStrings.add(similarAttributesString);
                    attributeStrings.add(getLineSeparator());
                } else {
                    similarAttributes
                        .stream()
                        .map(this::createLinkableItemString)
                        .map(str -> getListItemPrefix() + str + getLineSeparator())
                        .forEach(attributeStrings::add);
                }
            }
        }
        return attributeStrings;
    }

    private List<String> createCommonComponentMessagePieces(Collection<ComponentItem> componentItems) {
        Optional<ComponentItem> optionalArbitraryItem = getArbitraryElement(componentItems);
        List<String> commonComponentMessagePieces = new LinkedList<>();
        if (optionalArbitraryItem.isPresent()) {
            ComponentItem arbitraryItem = optionalArbitraryItem.get();
            StringBuilder componentItemBuilder = new StringBuilder()
                                                     .append(arbitraryItem.getCategory())
                                                     .append(" (")
                                                     .append(arbitraryItem.getOperation())
                                                     .append(") - ")
                                                     .append(createLinkableItemValueString(arbitraryItem.getComponent()))
                                                     .append(' ');
            arbitraryItem
                .getSubComponent()
                .map(this::createLinkableItemValueString)
                .map(str -> String.format("[%s]", str))
                .ifPresent(componentItemBuilder::append);
            componentItemBuilder.append(getLineSeparator());

            commonComponentMessagePieces.add(componentItemBuilder.toString());
            commonComponentMessagePieces.addAll(createComponentAttributeMessagePieces(componentItems));
        }
        return commonComponentMessagePieces;
    }

    private List<String> createCollapsedCategoryItemPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        SetMap<String, ComponentItem> groupedAndPrioritizedItems = groupAndPrioritizeCollapsibleItems(componentItems);

        for (Set<ComponentItem> itemGroup : groupedAndPrioritizedItems.values()) {
            componentItemPieces.add(getLineSeparator());
            Optional<ComponentItem> optionalGroupedItem = getArbitraryElement(itemGroup);
            if (optionalGroupedItem.isPresent()) {
                ComponentItem groupedItem = optionalGroupedItem.get();
                createCategoryGroupingString(groupedItem)
                    .map(str -> str + getLineSeparator())
                    .ifPresent(componentItemPieces::add);

                String categoryItemNameString = encodeString(groupedItem.getCategoryItem().getName() + ": ");
                componentItemPieces.add(categoryItemNameString);

                Set<LinkableItem> categoryItems = itemGroup
                                                      .stream()
                                                      .map(ComponentItem::getCategoryItem)
                                                      .collect(Collectors.toSet());
                componentItemPieces.addAll(createLinkableItemValuesPieces(categoryItems));
                componentItemPieces.add(getLineSeparator());
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

            String categoryString = createLinkableItemString(componentItem.getCategoryItem());
            componentItemPieces.add(categoryString + getLineSeparator());
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
