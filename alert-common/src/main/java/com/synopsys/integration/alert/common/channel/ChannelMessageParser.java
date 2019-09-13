package com.synopsys.integration.alert.common.channel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public abstract class ChannelMessageParser {
    public List<String> createMessagePieces(MessageContentGroup messageContentGroup) {
        LinkedList<String> messagePieces = new LinkedList<>();

        String commonTopicString = createLinkableItemString(messageContentGroup.getCommonTopic(), true);
        messagePieces.add(commonTopicString);

        for (ProviderMessageContent messageContent : messageContentGroup.getSubContent()) {
            messageContent.getSubTopic()
                .map(item -> createLinkableItemString(item, true))
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

    public List<String> createComponentItemMessagePieces(Set<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        boolean collapseOnCategory = componentItems
                                         .stream()
                                         .allMatch(ComponentItem::collapseOnCategory);
        if (collapseOnCategory) {
            List<String> collapsedComponentPieces = createCollapsedComponentPieces(componentItems);
            componentItemPieces.addAll(collapsedComponentPieces);
        } else {
            List<String> nonCollapsibleComponentPieces = createNonCollapsibleComponentPieces(componentItems);
            componentItemPieces.addAll(nonCollapsibleComponentPieces);
        }
        return componentItemPieces;
    }

    public String createCommonComponentItemString(ComponentItem componentItem) {
        StringBuilder componentItemBuilder = new StringBuilder();
        componentItemBuilder.append("Category: ");
        componentItemBuilder.append(componentItem.getCategory());
        componentItemBuilder.append(getLineSeparator());
        componentItemBuilder.append("Operation: ");
        componentItemBuilder.append(componentItem.getOperation());
        componentItemBuilder.append(getLineSeparator());
        componentItemBuilder.append(createLinkableItemString(componentItem.getComponent()));
        componentItem
            .getSubComponent()
            .map(this::createLinkableItemString)
            .ifPresent(componentItemBuilder::append);
        return componentItemBuilder.toString();
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

        return formattedString + getLineSeparator();
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

    protected abstract String encodeString(String txt);

    protected abstract String emphasize(String txt);

    protected abstract String createLink(String txt, String url);

    protected abstract String getLineSeparator();

    protected String getTopicSectionSeparator() {
        return "- - - - - - - - - - - - - - - - - - - -";
    }

    private List<String> createCollapsedComponentPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();

        Optional<ComponentItem> optionalArbitraryItem = componentItems
                                                            .stream()
                                                            .findAny();
        if (optionalArbitraryItem.isPresent()) {
            ComponentItem arbitraryItem = optionalArbitraryItem.get();
            String commonComponentItemString = createCommonComponentItemString(arbitraryItem);
            componentItemPieces.add(commonComponentItemString + getLineSeparator());

            SetMap<String, ComponentItem> groupedItems = groupAndPrioritizeCollapsibleItems(componentItems);
            for (Map.Entry<String, Set<ComponentItem>> groupedItemsEntry : groupedItems.entrySet()) {
                Optional<ComponentItem> optionalGroupedItem = groupedItemsEntry.getValue()
                                                                  .stream()
                                                                  .findAny();

                if (optionalGroupedItem.isPresent()) {
                    ComponentItem arbitraryGroupedItem = optionalGroupedItem.get();
                    String categoryGroupingString = createCategoryGroupingString(arbitraryGroupedItem);
                    componentItemPieces.add(categoryGroupingString);

                    groupedItemsEntry.getValue()
                        .stream()
                        .map(ComponentItem::getComponentAttributes)
                        .flatMap(Set::stream)
                        .distinct()
                        .map(this::createLinkableItemString)
                        .forEach(componentItemPieces::add);

                    String categoryItemNameString = encodeString(arbitraryGroupedItem.getCategoryItem().getName() + ": ");
                    componentItemPieces.add(categoryItemNameString);

                    Set<LinkableItem> categoryItems = groupedItemsEntry.getValue()
                                                          .stream()
                                                          .map(ComponentItem::getCategoryItem)
                                                          .collect(Collectors.toSet());
                    for (LinkableItem categoryItem : categoryItems) {
                        String linkableItemValueString = createLinkableItemValueString(categoryItem);
                        componentItemPieces.add("[" + linkableItemValueString + "]");
                    }
                    componentItemPieces.add(getLineSeparator() + getLineSeparator());
                }
            }
        } else {
            componentItemPieces.add("No content");
        }
        return componentItemPieces;
    }

    private List<String> createNonCollapsibleComponentPieces(Collection<ComponentItem> componentItems) {
        List<String> componentItemPieces = new LinkedList<>();
        for (ComponentItem componentItem : componentItems) {
            String componentItemString = createCommonComponentItemString(componentItem);
            componentItemPieces.add(componentItemString);

            componentItemPieces.add(createLinkableItemString(componentItem.getCategoryItem()));
            componentItemPieces.add(createCategoryGroupingString(componentItem));

            Set<LinkableItem> componentAttributes = componentItem.getComponentAttributes();
            for (LinkableItem attribute : componentAttributes) {
                String attributeString = createLinkableItemString(attribute);
                componentItemPieces.add(attributeString);
            }
            componentItemPieces.add(getLineSeparator() + getLineSeparator());
        }
        return componentItemPieces;
    }

    private String createCategoryGroupingString(ComponentItem componentItem) {
        return componentItem
                   .getCategoryGroupingAttribute()
                   .map(this::createLinkableItemString)
                   .orElse(StringUtils.EMPTY);
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

}
