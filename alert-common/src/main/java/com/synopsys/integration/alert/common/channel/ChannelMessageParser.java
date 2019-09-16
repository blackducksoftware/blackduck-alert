package com.synopsys.integration.alert.common.channel;

import java.util.Collection;
import java.util.LinkedHashMap;
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
                List<String> componentItemMessagePieces = createComponentItemMessagePieces(similarItems);
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

    protected List<String> createComponentItemMessagePieces(Set<ComponentItem> componentItems) {
        List<String> componentItemPieces = createCommonComponentMessagePieces(componentItems);
        boolean collapseOnCategory = componentItems.stream().allMatch(ComponentItem::collapseOnCategory);
        if (collapseOnCategory) {
            List<String> collapsedComponentPieces = createCollapsedComponentPieces(componentItems);
            componentItemPieces.addAll(collapsedComponentPieces);
        } else {
            List<String> nonCollapsibleComponentPieces = createNonCollapsibleComponentPieces(componentItems);
            componentItemPieces.addAll(nonCollapsibleComponentPieces);
        }
        return componentItemPieces;
    }

    protected abstract String encodeString(String txt);

    protected abstract String emphasize(String txt);

    protected abstract String createLink(String txt, String url);

    protected abstract String getLineSeparator();

    protected String getTopicSectionSeparator() {
        return "- - - - - - - - - - - - - - - - - - - -";
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
                                                         .collect(SetMap::new, (map, item) -> map.add(item.getName(), item), SetMap::combine);

        List<String> attributeStrings = new LinkedList<>();
        for (Set<LinkableItem> similarAttributes : attributesMap.values()) {
            Optional<LinkableItem> optionalAttribute = similarAttributes
                                                           .stream()
                                                           .findAny();
            if (optionalAttribute.isPresent()) {
                LinkableItem attribute = optionalAttribute.get();
                if (similarAttributes.size() == 1) {
                    attributeStrings.add(createLinkableItemString(attribute) + getLineSeparator());
                } else {
                    List<String> valuePieces = createLinkableItemValuesPieces(similarAttributes);
                    String valueString = String.join("", valuePieces);
                    String similarAttributesString = String.format("%s: %s", attribute.getName(), valueString);
                    attributeStrings.add(similarAttributesString + getLineSeparator());
                }
            }
        }
        return attributeStrings;
    }

    private List<String> createCommonComponentMessagePieces(Collection<ComponentItem> componentItems) {
        Optional<ComponentItem> optionalArbitraryItem = componentItems
                                                            .stream()
                                                            .findAny();
        List<String> commonComponentMessagePieces = new LinkedList<>();
        if (optionalArbitraryItem.isPresent()) {
            ComponentItem arbitraryItem = optionalArbitraryItem.get();
            StringBuilder componentItemBuilder = new StringBuilder();
            componentItemBuilder.append("Category: ");
            componentItemBuilder.append(arbitraryItem.getCategory());
            componentItemBuilder.append(getLineSeparator());
            componentItemBuilder.append("Operation: ");
            componentItemBuilder.append(arbitraryItem.getOperation());
            componentItemBuilder.append(getLineSeparator());
            componentItemBuilder.append(createLinkableItemString(arbitraryItem.getComponent()));
            componentItemBuilder.append(getLineSeparator());
            arbitraryItem
                .getSubComponent()
                .map(this::createLinkableItemString)
                .map(str -> str + getLineSeparator())
                .ifPresent(componentItemBuilder::append);

            commonComponentMessagePieces.add(componentItemBuilder.toString());
            commonComponentMessagePieces.addAll(createComponentAttributeMessagePieces(componentItems));
        }
        return commonComponentMessagePieces;
    }

    private List<String> createCollapsedComponentPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        SetMap<String, ComponentItem> groupedItems = groupAndPrioritizeCollapsibleItems(componentItems);
        for (Map.Entry<String, Set<ComponentItem>> groupedItemsEntry : groupedItems.entrySet()) {
            Optional<ComponentItem> optionalGroupedItem = groupedItemsEntry.getValue()
                                                              .stream()
                                                              .findAny();
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

    private List<String> createNonCollapsibleComponentPieces(Collection<ComponentItem> componentItems) {
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
        Map<String, Set<ComponentItem>> initializer = new LinkedHashMap<>();
        SetMap<String, ComponentItem> groupedAndPrioritizedItems = new SetMap<>(initializer);
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

}
