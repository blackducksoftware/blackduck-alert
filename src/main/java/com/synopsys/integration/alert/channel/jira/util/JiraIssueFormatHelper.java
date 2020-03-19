/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.jira.model.IssueContentModel;
import com.synopsys.integration.alert.common.channel.MessageSplitter;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentKeys;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class JiraIssueFormatHelper {
    public static final int TITLE_LIMIT = 255;
    private static final int TEXT_LIMIT = 30000;
    private static final String LINE_SEPARATOR = "\n";

    public String createTitle(final String provider, final LinkableItem topic, final Optional<LinkableItem> subTopic, final ComponentKeys componentKeys) {
        final StringBuilder title = new StringBuilder();
        title.append("Alert - Provider: ");
        title.append(provider);
        title.append(", ");
        title.append(topic.getName());
        title.append(": ");
        title.append(topic.getValue());

        if (subTopic.isPresent()) {
            final LinkableItem subTopicItem = subTopic.get();
            title.append(", ");
            title.append(subTopicItem.getName());
            title.append(": ");
            title.append(subTopicItem.getValue());
            title.append(", ");
        }

        // FIXME make this provider-agnostic
        String prettyPrintedKey;
        if (!componentKeys.getCategory().contains("Vuln")) {
            prettyPrintedKey = componentKeys.prettyPrint(true);
        } else {
            prettyPrintedKey = componentKeys.prettyPrint(false);
        }
        title.append(prettyPrintedKey);
        return StringUtils.abbreviate(title.toString(), TITLE_LIMIT);
    }

    public IssueContentModel createDescription(LinkableItem commonTopic, Optional<LinkableItem> subTopic, Collection<ComponentItem> componentItems, String providerName, ComponentKeys componentKeys) {
        final String title = createTitle(providerName, commonTopic, subTopic, componentKeys);
        final StringBuilder description = new StringBuilder();
        description.append("Provider: ");
        description.append(providerName);
        description.append(LINE_SEPARATOR);
        description.append(commonTopic.getName());
        description.append(": ");
        description.append(commonTopic.getValue());
        description.append(LINE_SEPARATOR);
        if (subTopic.isPresent()) {
            final LinkableItem linkableItem = subTopic.get();
            String valueString = createValueString(linkableItem);
            description.append(linkableItem.getName());
            description.append(": ");
            description.append(valueString);
            description.append(LINE_SEPARATOR);
        }

        final Optional<ComponentItem> arbitraryItem = componentItems
                                                          .stream()
                                                          .findAny();
        List<String> additionalComments = new ArrayList<>();
        List<String> descriptionAttributes = new ArrayList<>();
        if (arbitraryItem.isPresent()) {
            String componentSection = createComponentString(arbitraryItem.get());
            description.append(componentSection);

            splitComponentAttributesForDescription(description.length(), componentItems, descriptionAttributes, additionalComments);
            description.append(StringUtils.join(descriptionAttributes, LINE_SEPARATOR));
        }
        return IssueContentModel.of(title, description.toString(), additionalComments);
    }

    public void splitComponentAttributesForDescription(int descriptionLength, Collection<ComponentItem> componentItems, Collection<String> descriptionAttributes, Collection<String> additionalComments) {
        Set<String> descriptionItems = new LinkedHashSet<>();
        MessageSplitter splitter = new MessageSplitter(TEXT_LIMIT, LINE_SEPARATOR);
        for (ComponentItem componentItem : componentItems) {
            final Set<String> descriptionItemsForComponent = createDescriptionItems(componentItem);
            descriptionItems.addAll(descriptionItemsForComponent);
        }

        int currentLength = descriptionLength;
        List<String> tempAdditionalComments = new ArrayList<>();
        for (String descriptionItem : descriptionItems) {
            int itemLength = descriptionItem.length();
            if (currentLength >= TEXT_LIMIT) {
                tempAdditionalComments.add(descriptionItem);
            } else if (itemLength + currentLength >= TEXT_LIMIT) {
                tempAdditionalComments.add(descriptionItem);
                currentLength = currentLength + descriptionItem.length();
            } else {
                descriptionAttributes.add(descriptionItem);
                // add one for the newline character.
                currentLength = 1 + currentLength + descriptionItem.length();
            }
        }
        additionalComments.addAll(splitter.splitMessages(tempAdditionalComments, true));
    }

    public List<String> createOperationComment(ItemOperation operation, String category, String provider, Collection<ComponentItem> componentItems) {
        String attributesString = createComponentAttributesString(componentItems);
        Collection<String> text = new ArrayList<>();
        String description = String.format("The %s operation was performed for this %s in %s.", operation.name(), category, provider);
        text.add(description);
        if (StringUtils.isNotBlank(attributesString)) {
            text.add("\n----------\n");
            text.add(attributesString);
        }
        MessageSplitter splitter = new MessageSplitter(TEXT_LIMIT, LINE_SEPARATOR);
        return splitter.splitMessages(text, true);
    }

    private String createComponentAttributesString(Collection<ComponentItem> componentItems) {
        Set<String> descriptionItems = new LinkedHashSet<>();

        for (ComponentItem componentItem : componentItems) {
            final Set<String> descriptionItemsForComponent = createDescriptionItems(componentItem);
            descriptionItems.addAll(descriptionItemsForComponent);
        }

        StringBuilder attributes = new StringBuilder();
        for (String descriptionItem : descriptionItems) {
            attributes.append(descriptionItem);
            attributes.append(LINE_SEPARATOR);
        }

        return attributes.toString();
    }

    private String createComponentString(ComponentItem componentItem) {
        StringBuilder componentSection = new StringBuilder();
        componentSection.append("Category: ");
        componentSection.append(componentItem.getCategory());
        componentSection.append(LINE_SEPARATOR);

        LinkableItem component = componentItem.getComponent();
        componentSection.append(component.getName());
        componentSection.append(": ");
        componentSection.append(component.getValue());

        componentItem.getSubComponent().ifPresent(subComponent -> {
            componentSection.append(LINE_SEPARATOR);
            componentSection.append(subComponent.getName());
            componentSection.append(": ");
            componentSection.append(createValueString(subComponent));
        });

        componentSection.append(LINE_SEPARATOR);
        return componentSection.toString();
    }

    private Set<String> createDescriptionItems(ComponentItem componentItem) {
        Set<String> descriptionItems = new LinkedHashSet<>();
        Map<String, List<LinkableItem>> itemsOfSameName = componentItem.getItemsOfSameName();

        for (final Map.Entry<String, List<LinkableItem>> entry : itemsOfSameName.entrySet()) {
            String itemName = entry.getKey();
            String valuesString = createValuesString(entry.getValue());

            String descriptionItem = String.format("%s: %s ", itemName, valuesString);
            descriptionItems.add(descriptionItem);
        }

        return descriptionItems;
    }

    private String createValuesString(Collection<LinkableItem> linkableItems) {
        if (linkableItems.size() == 1) {
            final LinkableItem item = linkableItems
                                          .stream()
                                          .findAny()
                                          .orElseThrow(() -> new AlertRuntimeException("A non-empty list had no elements"));
            return createValueString(item);
        } else {
            StringBuilder valuesBuilder = new StringBuilder();
            for (LinkableItem item : linkableItems) {
                final String valueString = createValueString(item);
                valuesBuilder.append("[ ");
                valuesBuilder.append(valueString);
                valuesBuilder.append(" ] ");
            }
            return valuesBuilder.toString();
        }
    }

    private String createValueString(LinkableItem linkableItem) {
        StringBuilder valueBuilder = new StringBuilder();
        final Optional<String> url = linkableItem.getUrl();
        if (url.isPresent()) {
            valueBuilder.append('[');
            valueBuilder.append(linkableItem.getValue());
            valueBuilder.append('|');
            valueBuilder.append(url.get());
            valueBuilder.append(']');
        } else {
            valueBuilder.append(linkableItem.getValue());
        }
        valueBuilder.append(' ');
        return valueBuilder.toString();
    }

}
