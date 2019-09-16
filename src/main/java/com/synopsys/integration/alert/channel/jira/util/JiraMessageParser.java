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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.model.IssueContentModel;
import com.synopsys.integration.alert.common.channel.ChannelMessageParser;
import com.synopsys.integration.alert.common.channel.MessageSplitter;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class JiraMessageParser extends ChannelMessageParser {
    public static final int TITLE_LIMIT = 255;
    private static final int TEXT_LIMIT = 30000;
    private static final String LINE_SEPARATOR = "\n";

    public IssueContentModel createIssueContentModel(ProviderMessageContent providerMessageContent, ComponentItem arbitraryItem) {
        String providerName = providerMessageContent.getProvider().getValue();
        LinkableItem messageTopic = providerMessageContent.getTopic();
        LinkableItem nullableSubTopic = providerMessageContent.getSubTopic().orElse(null);

        String title = createTitle(providerName, messageTopic, nullableSubTopic, arbitraryItem);

        StringBuilder description = new StringBuilder();
        description.append("Provider: ");
        description.append(providerName);
        description.append(LINE_SEPARATOR);
        description.append(messageTopic.getName());
        description.append(": ");
        description.append(messageTopic.getValue());
        description.append(LINE_SEPARATOR);
        if (null != nullableSubTopic) {
            String valueString = createLinkableItemValueString(nullableSubTopic);
            description.append(nullableSubTopic);
            description.append(": ");
            description.append(valueString);
            description.append(LINE_SEPARATOR);
        }

        List<String> additionalComments = new ArrayList<>();
        List<String> descriptionAttributes = new ArrayList<>();
        String componentSection = createCommonComponentItemString(arbitraryItem);
        description.append(componentSection);

        splitAdditionalComponentInfoForDescription(description.length(), providerMessageContent, descriptionAttributes, additionalComments);
        description.append(StringUtils.join(descriptionAttributes, LINE_SEPARATOR));
        return IssueContentModel.of(title, description.toString(), additionalComments);
    }

    public List<String> createOperationComment(String provider, String category, ItemOperation operation, Collection<ComponentItem> componentItems) {
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
        Set<String> descriptionItems = componentItems
                                           .stream()
                                           .map(ComponentItem::getComponentAttributes)
                                           .flatMap(Set::stream)
                                           .distinct()
                                           .map(this::createLinkableItemString)
                                           .collect(Collectors.toSet());

        StringBuilder attributes = new StringBuilder();
        for (String descriptionItem : descriptionItems) {
            attributes.append(descriptionItem);
            attributes.append(LINE_SEPARATOR);
        }

        return attributes.toString();
    }

    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        // TODO emphasize?
        return txt;
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("[%s|%s]", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

    private String createTitle(String provider, LinkableItem topic, LinkableItem subTopic, ComponentItem arbitraryItem) {
        StringBuilder title = new StringBuilder();
        title.append("Alert - Provider: ");
        title.append(provider);
        title.append(createTitlePartStringPrefixedWithComma(topic));

        if (null != subTopic) {
            title.append(createTitlePartStringPrefixedWithComma(subTopic));
        }

        title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getComponent()));
        arbitraryItem
            .getSubComponent()
            .ifPresent(linkableItem -> title.append(createTitlePartStringPrefixedWithComma(linkableItem)));

        if (arbitraryItem.collapseOnCategory()) {
            title.append(", ");
            title.append(arbitraryItem.getCategory());
        } else {
            title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getCategoryItem()));
        }

        return StringUtils.abbreviate(title.toString(), TITLE_LIMIT);
    }

    private void splitAdditionalComponentInfoForDescription(int descriptionLength, ProviderMessageContent messageContent, Collection<String> descriptionAttributes, Collection<String> additionalComments) {
        MessageSplitter splitter = new MessageSplitter(TEXT_LIMIT, LINE_SEPARATOR);
        List<String> componentItemMessagePieces = createAggregateComponentMessagePieces(messageContent);

        int currentLength = descriptionLength;
        List<String> tempAdditionalComments = new ArrayList<>();
        for (String descriptionItem : componentItemMessagePieces) {
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

    private String createTitlePartStringPrefixedWithComma(LinkableItem linkableItem) {
        return String.format(", %s: %s", linkableItem.getName(), linkableItem.getValue());
    }

}
