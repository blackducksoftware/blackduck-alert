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
package com.synopsys.integration.alert.common.channel.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.datastructure.SetMap;

public abstract class ChannelMessageParser {
    private static final String LIST_ITEM_PREFIX = "- ";

    public String createMessage(MessageContentGroup messageContentGroup) {
        List<String> messagePieces = createMessagePieces(messageContentGroup);
        return String.join("", messagePieces);
    }

    public List<String> createMessagePieces(MessageContentGroup messageContentGroup) {
        LinkedList<String> messagePieces = new LinkedList<>();
        String header = createHeader(messageContentGroup);
        if (StringUtils.isNotBlank(header)) {
            messagePieces.add(header + getLineSeparator());
        }

        ItemOperation nullableTopLevelAction = getNullableTopLevelAction(messageContentGroup).orElse(null);
        String commonTopicString = getCommonTopic(messageContentGroup, nullableTopLevelAction);
        messagePieces.add(commonTopicString);

        for (ProviderMessageContent messageContent : messageContentGroup.getSubContent()) {
            String componentSubTopic = getComponentSubTopic(messageContent, nullableTopLevelAction);
            if (StringUtils.isNotBlank(componentSubTopic)) {
                messagePieces.add(componentSubTopic + getLineSeparator());
            }

            List<String> componentItems = createComponentItemMessage(messageContent);
            messagePieces.addAll(componentItems);
        }

        String footer = createFooter();
        if (StringUtils.isNotBlank(footer)) {
            messagePieces.add(footer + getLineSeparator());
        }
        return messagePieces;
    }

    public String createHeader(MessageContentGroup messageContentGroup) {
        String messageHeader = String.format("Begin %s Content", messageContentGroup.getCommonProvider().getValue());
        return createMessageSeparator(messageHeader);
    }

    public String getCommonTopic(MessageContentGroup messageContent, @Nullable ItemOperation nullableTopLevelAction) {
        LinkableItem commonProject = messageContent.getCommonProject();
        if (ItemOperation.DELETE.equals(nullableTopLevelAction)) {
            commonProject = new LinkableItem(commonProject.getName(), commonProject.getValue());
        }
        return createLinkableItemString(commonProject, true) + getLineSeparator();
    }

    public String getComponentSubTopic(ProviderMessageContent messageContent, @Nullable ItemOperation nullableTopLevelAction) {
        Optional<LinkableItem> optionalSubTopic = messageContent.getProjectVersion();
        if (optionalSubTopic.isPresent()) {
            LinkableItem subTopic = optionalSubTopic.get();
            if (ItemOperation.DELETE.equals(nullableTopLevelAction)) {
                subTopic = new LinkableItem(subTopic.getName(), subTopic.getValue());
            }
            return createLinkableItemString(subTopic, true);
        }
        return StringUtils.EMPTY;
    }

    public List<String> createComponentItemMessage(ProviderMessageContent messageContent) {
        List<String> messagePieces = new LinkedList<>();
        if (messageContent.isTopLevelActionOnly()) {
            messageContent
                .getAction()
                .map(ItemOperation::name)
                .map(action -> String.format("%s Action: %s%s", messageContent.getProject().getName(), action, getLineSeparator()))
                .ifPresent(messagePieces::add);
        } else {
            SetMap<String, ComponentItem> componentItemSetMap = messageContent.groupRelatedComponentItems();
            for (Set<ComponentItem> similarItems : componentItemSetMap.values()) {
                messagePieces.add(getSectionSeparator() + getLineSeparator());
                List<String> componentItemMessagePieces = createComponentAndCategoryMessagePieces(similarItems);
                messagePieces.addAll(componentItemMessagePieces);
            }
            messagePieces.add(getLineSeparator());
        }
        return messagePieces;
    }

    public String createFooter() {
        return createMessageSeparator("End Content");
    }

    protected abstract String encodeString(String txt);

    protected abstract String emphasize(String txt);

    protected abstract String createLink(String txt, String url);

    protected abstract String getLineSeparator();

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

    protected String createLinkableItemString(LinkableItem linkableItem, boolean bold) {
        String name = encodeString(linkableItem.getName());
        String value = encodeString(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        if (bold) {
            name = emphasize(name);
            value = emphasize(value);
        }

        if (optionalUrl.isPresent()) {
            // The nuance around stylizing links adds too much complexity for too little value to worry about emphasizing them.
            value = createLinkableItemValueString(linkableItem);
        }

        return String.format("%s: %s", name, value);
    }

    protected List<String> createComponentAttributeMessagePieces(Collection<ComponentItem> componentItems) {
        SetMap<String, LinkableItem> attributesMap = componentItems
                                                         .stream()
                                                         .map(ComponentItem::getComponentAttributes)
                                                         .flatMap(Set::stream)
                                                         .collect(SetMap::createLinked, (map, item) -> map.add(item.getName(), item), SetMap::combine);
        List<String> attributeStrings = new LinkedList<>();
        for (Set<LinkableItem> similarAttributes : attributesMap.values()) {
            Optional<LinkableItem> optionalAttribute = getArbitraryElement(similarAttributes);
            if (optionalAttribute.isPresent()) {
                LinkableItem attribute = optionalAttribute.get();
                if (attribute.isCollapsible()) {
                    List<String> valuePieces = createLinkableItemValuesPieces(similarAttributes);
                    String valueString = String.join("", valuePieces);
                    String similarAttributesString = String.format("%s%s: %s", LIST_ITEM_PREFIX, attribute.getName(), valueString);
                    attributeStrings.add(similarAttributesString);
                    attributeStrings.add(getLineSeparator());
                } else {
                    similarAttributes
                        .stream()
                        .map(this::createLinkableItemString)
                        .map(str -> LIST_ITEM_PREFIX + str + getLineSeparator())
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
                LinkableItem categoryItem = groupedItem.getCategoryItem();
                Optional<LinkableItem> optionalGroupingAttribute = groupedItem.getCategoryGroupingAttribute();

                String categoryItemNameString;
                if (optionalGroupingAttribute.isPresent()) {
                    LinkableItem groupedCategoryItem = createGroupedCategoryItem(categoryItem, optionalGroupingAttribute.get());
                    categoryItemNameString = groupedCategoryItem.getName();
                } else {
                    categoryItemNameString = categoryItem.getName();
                }
                componentItemPieces.add(encodeString(categoryItemNameString + ": "));

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
            LinkableItem categoryItem = componentItem.getCategoryItem();
            Optional<LinkableItem> optionalGroupingAttribute = componentItem.getCategoryGroupingAttribute();
            String categoryString;
            if (optionalGroupingAttribute.isPresent()) {
                LinkableItem groupedCategoryItem = createGroupedCategoryItem(categoryItem, optionalGroupingAttribute.get());
                categoryString = createLinkableItemString(groupedCategoryItem);
            } else {
                categoryString = createLinkableItemString(categoryItem);
            }
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

    private LinkableItem createGroupedCategoryItem(LinkableItem categoryItem, LinkableItem categoryGroupingAttribute) {
        String groupedCategoryName = String.format("%s (%s)", categoryItem.getName(), categoryGroupingAttribute.getValue());
        return new LinkableItem(groupedCategoryName, categoryItem.getValue(), categoryItem.getUrl().orElse(null));
    }

    private String createLinkableItemString(LinkableItem linkableItem) {
        return createLinkableItemString(linkableItem, false);
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

    protected String createLinkableItemValueString(LinkableItem linkableItem) {
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

    protected Optional<ItemOperation> getNullableTopLevelAction(MessageContentGroup messageContentGroup) {
        return messageContentGroup.getSubContent()
                   .stream()
                   .filter(ProviderMessageContent::isTopLevelActionOnly)
                   .map(ProviderMessageContent::getAction)
                   .flatMap(Optional::stream)
                   .findAny();
    }

}
