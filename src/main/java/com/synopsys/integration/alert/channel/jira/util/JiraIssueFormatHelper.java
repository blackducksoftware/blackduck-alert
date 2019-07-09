/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentKeys;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class JiraIssueFormatHelper {

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
        return title.toString();
    }

    public String createDescription(final LinkableItem commonTopic, final Optional<LinkableItem> subTopic, final Collection<ComponentItem> componentItems, final String providerName) {
        final StringBuilder description = new StringBuilder();
        description.append("Provider: ");
        description.append(providerName);
        description.append("\n");
        description.append(commonTopic.getName());
        description.append(": ");
        description.append(commonTopic.getValue());
        description.append("\n");
        if (subTopic.isPresent()) {
            final LinkableItem linkableItem = subTopic.get();
            description.append(linkableItem.getName());
            description.append(": ");
            String valueString = createValueString(linkableItem);
            description.append(valueString);
            description.append("\n");
        }

        final Optional<ComponentItem> arbitraryItem = componentItems
                                                          .stream()
                                                          .findAny();
        if (arbitraryItem.isPresent()) {
            String componentSection = createComponentString(arbitraryItem.get());
            description.append(componentSection);

            final String componentAttributesSection = createComponentAttributesString(componentItems);
            description.append(componentAttributesSection);
        }

        return description.toString();
    }

    public String createComponentAttributesString(Collection<ComponentItem> componentItems) {
        Set<String> descriptionItems = new LinkedHashSet<>();

        for (ComponentItem componentItem : componentItems) {
            final Set<String> descriptionItemsForComponent = createDescriptionItems(componentItem);
            descriptionItems.addAll(descriptionItemsForComponent);
        }

        StringBuilder attributes = new StringBuilder();
        for (String descriptionItem : descriptionItems) {
            attributes.append(descriptionItem);
            attributes.append("\n");
        }

        return attributes.toString();
    }

    private String createComponentString(ComponentItem componentItem) {
        StringBuilder componentSection = new StringBuilder();
        componentSection.append("Category: ");
        componentSection.append(componentItem.getCategory());
        componentSection.append("\n");

        LinkableItem component = componentItem.getComponent();
        componentSection.append(component.getName());
        componentSection.append(": ");
        componentSection.append(component.getValue());

        componentItem.getSubComponent().ifPresent(subComponent -> {
            componentSection.append("\n");
            componentSection.append(subComponent.getName());
            componentSection.append(": ");
            componentSection.append(subComponent.getValue());
        });

        componentSection.append("\n");
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
