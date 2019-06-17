package com.synopsys.integration.alert.common.workflow.processor2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentKey;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;

@Component
public class DefaultMessageContentProcessor extends MessageContentProcessor {

    @Autowired
    public DefaultMessageContentProcessor() {
        super(FormatType.DEFAULT);
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        final Map<ContentKey, List<ProviderMessageContent>> messagesGroupedByKey = new LinkedHashMap<>();
        for (ProviderMessageContent message : messages) {
            messagesGroupedByKey.computeIfAbsent(message.getContentKey(), k -> new LinkedList<>()).add(message);
        }

        final Map<ContentKey, MessageContentGroup> messageGroups = new LinkedHashMap<>();
        for (final Map.Entry<ContentKey, List<ProviderMessageContent>> groupedMessageEntry : messagesGroupedByKey.entrySet()) {
            final List<ProviderMessageContent> groupedMessages = groupedMessageEntry.getValue();
            final Set<ComponentItem> combinedComponentItems = gatherComponentItems(groupedMessages);

            final Optional<ProviderMessageContent> arbitraryMessage = groupedMessages
                                                                          .stream()
                                                                          .findAny();
            if (arbitraryMessage.isPresent()) {
                try {
                    final ProviderMessageContent newMessage = createNewMessage(arbitraryMessage.get(), combinedComponentItems);
                    messageGroups.computeIfAbsent(groupedMessageEntry.getKey(), ignored -> new MessageContentGroup()).add(newMessage);
                } catch (AlertException e) {
                    // FIXME handle exception
                }
            }
        }

        return new ArrayList<>(messageGroups.values());
    }

    private SortedSet<ComponentItem> gatherComponentItems(final List<ProviderMessageContent> groupedMessages) {
        final List<ComponentItem> allCategoryItems = groupedMessages
                                                         .stream()
                                                         .map(ProviderMessageContent::getComponentItems)
                                                         .flatMap(Set::stream)
                                                         .collect(Collectors.toList());
        return combineCategoryItems(allCategoryItems);
    }

    // FIXME do the sets still need to be sorted?
    private SortedSet<ComponentItem> combineCategoryItems(final List<ComponentItem> allComponentItems) {
        // The amount of collapsing we do makes this impossible to map back to a single notification.
        final Map<ComponentKey, ComponentItem> keyToItems = new LinkedHashMap<>();
        for (final ComponentItem componentItem : allComponentItems) {
            final ComponentKey componentKey = generateCategoryKey(componentItem);
            final ComponentItem oldItem = keyToItems.get(componentKey);

            // Always use the newest notification because the audit entry will appear first.
            final Long notificationId = componentItem.getNotificationId();
            final Set<LinkableItem> linkableItems;
            if (null != oldItem) {
                linkableItems = combineLinkableItems(oldItem.getComponentAttributes(), componentItem.getComponentAttributes());
            } else {
                linkableItems = componentItem.getComponentAttributes();
            }

            ComponentItem newComponentItem = null;
            try {
                newComponentItem = createNewComponentItem(componentItem, componentItem.getOperation(), notificationId, linkableItems);
                // FIXME newComponentItem.setComparator(componentItem.createComparator());
                keyToItems.put(componentKey, newComponentItem);
            } catch (AlertException e) {
                // FIXME handle exception
            }
        }
        return new TreeSet<>(keyToItems.values());
    }

    private ComponentKey generateCategoryKey(final ComponentItem componentItem) {
        final String addtionalDataString = ComponentKey.generateAdditionalDataString(componentItem.getComponentAttributes());
        final LinkableItem component = componentItem.getComponent();
        final String subComponentName = componentItem.getSubComponent().map(LinkableItem::getName).orElse(null);
        final String subComponentValue = componentItem.getSubComponent().map(LinkableItem::getValue).orElse(null);
        return new ComponentKey(componentItem.getCategory(), component.getName(), component.getValue(), subComponentName, subComponentValue, addtionalDataString);
    }

    private SortedSet<LinkableItem> combineLinkableItems(final Set<LinkableItem> oldItems, final Set<LinkableItem> newItems) {
        final SortedSet<LinkableItem> combinedItems = new TreeSet<>(oldItems);
        newItems
            .stream()
            .filter(LinkableItem::isCollapsible)
            .forEach(combinedItems::add);
        return combinedItems;
    }

}

