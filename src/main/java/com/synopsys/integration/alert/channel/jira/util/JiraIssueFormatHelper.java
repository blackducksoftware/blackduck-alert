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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (componentKeys.getCategory().contains("Policy")) {
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

        for (ComponentItem componentItem : componentItems) {
            description.append(createDescriptionItems(componentItem));
        }

        return description.toString();
    }

    private String createDescriptionItems(final ComponentItem componentItem) {
        final StringBuilder description = new StringBuilder();
        final Map<String, List<LinkableItem>> itemsOfSameName = componentItem.getItemsOfSameName();

        description.append('\n');
        for (final Map.Entry<String, List<LinkableItem>> entry : itemsOfSameName.entrySet()) {
            String itemName = entry.getKey();
            description.append(itemName);
            description.append(": ");
            String valuesString = createValuesString(entry.getValue());
            description.append(valuesString);

            description.append('\n');
        }

        return description.toString();
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
